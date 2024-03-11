package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.DuelGUI;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow public abstract ServerLevel getLevel();

    @Shadow public abstract void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g);

    @Shadow private int spawnInvulnerableTime;

    @Shadow public abstract void attack(Entity entity);

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ServerPlayerGameMode serverPlayerGameMode, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (FfaSkyUtil.isFfaPlayer(player)) {
            spawnInvulnerableTime = 0;
        }
    }
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void onAttack(Entity entity, CallbackInfo ci) {
        ServerPlayer attacker = (ServerPlayer) (Object) this;
        if(level == LobbyUtil.lobbyWorld && entity instanceof ServerPlayer player && player != attacker) {
            if(this.getItemInHand(InteractionHand.MAIN_HAND).getDisplayName().toString().toLowerCase().contains("queue sword")) DuelGUI.openDuelGui(attacker, player);
            if(this.getItemInHand(InteractionHand.MAIN_HAND).getDisplayName().toString().toLowerCase().contains("team axe")) PlayerUtil.getFactoryPlayer(attacker).runCommand("/party invite " + player.getScoreboardName());
            return;
        }

        if(level.equals(FootballGame.world) && FootballGame.isFootballPlayer(attacker) && entity instanceof ArmorStand) {

            if(!this.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.NETHERITE_SWORD)) {
                PlayerUtil.sendSound(attacker, SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 100, 1);
                ci.cancel();
                return;
            }
            if(attacker.getCooldowns().isOnCooldown(Items.NETHERITE_SWORD)) {
                PlayerUtil.sendSound(attacker, SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 100, 1);
                ci.cancel();
                return;
            }
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false, false));

            attacker.getCooldowns().addCooldown(Items.NETHERITE_SWORD, 200);

            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false, false));
            return;
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        PlayerGameMode gameMode = PlayerDataManager.get(player).gameMode;
        PlayerData duelsData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);


        if (FfaUtil.isFfaPlayer(player)) {
            FfaUtil.leaveOrDie(player, damageSource, false);
        } else if (BwAreas.isBedWarsWorld(getLevel())) {
            BwPlayerEvents.death(player);
        }
        else if(gameMode == PlayerGameMode.OITC){
            OitcGame.death(player, damageSource);
        }
        else if(gameMode == PlayerGameMode.SKYWARS) {
            SkywarsGame.death(player, damageSource);
        }
        else if(gameMode == PlayerGameMode.LOBBY && duelsData.duelsGame != null){
            duelsData.duelsGame.death(player, damageSource);
        } else if(gameMode == PlayerGameMode.LOBBY && duelsData.teamDuelsGame != null) {
            duelsData.teamDuelsGame.death(player, damageSource);
        }
    }

    @Redirect(method = "doCloseContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;removed(Lnet/minecraft/world/entity/player/Player;)V"))
    private void removed(AbstractContainerMenu instance, Player player) {

        if (FfaUtil.isFfaPlayer(player)) {
            player.inventory.add(inventory.getCarried());
            player.inventory.setCarried(ItemStack.EMPTY);
            return;
        }

        player.containerMenu.removed(player);
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void detect(CallbackInfo ci){
        List<Player> playersNearby = level.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(12, 0.25, 12));
        Vec3 eyePos = getEyePosition(1);
        AtomicReference<Vec3> nearestPosition = new AtomicReference<>();
        playersNearby.forEach(player -> {
            Vec3 currentPos = player.getBoundingBox().getNearestPointTo(eyePos);
            if(nearestPosition.get() == null || nearestPosition.get().distanceToSqr(eyePos) > currentPos.distanceToSqr(eyePos))
                nearestPosition.set(currentPos);
        });
        if(nearestPosition.get() != null) {
            Vec3 nearestPos = nearestPosition.get();
            ServerTime.factoryServer.runCommand("/player .bot look at " + nearestPos.x + " " + nearestPos.y + " " + nearestPos.z, 4, false);
        }
    }

}
