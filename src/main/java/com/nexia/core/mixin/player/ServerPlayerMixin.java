package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.commands.staff.DetectCommand;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.CustomDuelGUI;
import com.nexia.core.gui.duels.DuelGUI;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.players.BedwarsPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BedwarsUtil;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
        if (FfaUtil.isFfaPlayer(nexiaPlayer) || BedwarsUtil.isBedWarsPlayer(nexiaPlayer) || OitcGame.isOITCPlayer(nexiaPlayer)) {
            spawnInvulnerableTime = 0;
        }
    }
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void onAttack(Entity entity, CallbackInfo ci) {
        ServerPlayer attacker = (ServerPlayer) (Object) this;
        NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);

        if(level == LobbyUtil.lobbyWorld && entity instanceof ServerPlayer player && player != attacker) {
            String name = this.getItemInHand(InteractionHand.MAIN_HAND).getDisplayName().toString().toLowerCase();
            if(name.contains("custom duel sword")) {
                CustomDuelGUI.openDuelGui(attacker, player);
                return;
            }
            if(name.contains("duel sword")) {
                DuelGUI.openDuelGui(attacker, player);
                return;
            }
            if(name.contains("team axe")) {
                nexiaAttacker.runCommand("/party invite " + player.getScoreboardName());
                return;
            }
            return;
        }

        if(level.equals(FootballGame.world) && FootballGame.isFootballPlayer(nexiaAttacker) && entity instanceof ArmorStand) {

            if(!this.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.NETHERITE_SWORD)) {
                nexiaAttacker.sendSound(SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 100, 1);


                ci.cancel();
                return;
            }
            if(attacker.getCooldowns().isOnCooldown(Items.NETHERITE_SWORD)) {
                nexiaAttacker.sendSound(SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 100, 1);
                ci.cancel();
                return;
            }
            attacker.getCooldowns().addCooldown(Items.NETHERITE_SWORD, 200);
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false, false));
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
        PlayerGameMode gameMode = ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode;
        DuelsPlayerData duelsData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer);

        if (FfaUtil.isFfaPlayer(nexiaPlayer)) {
            FfaUtil.leaveOrDie(nexiaPlayer, damageSource, false);
        }
        else if (BedwarsAreas.isBedWarsWorld(getLevel())) {
            BedwarsPlayerEvents.death(nexiaPlayer );
        }
        else if(gameMode == PlayerGameMode.OITC){
            OitcGame.death(nexiaPlayer, damageSource);
        }
        else if(gameMode == PlayerGameMode.SKYWARS) {
            SkywarsGame.death(nexiaPlayer, damageSource);
        }
        else if(gameMode == PlayerGameMode.LOBBY && duelsData.gameOptions != null) {
            if(duelsData.gameOptions.duelsGame != null) duelsData.gameOptions.duelsGame.death(nexiaPlayer, damageSource);
            if(duelsData.gameOptions.teamDuelsGame != null) duelsData.gameOptions.teamDuelsGame.death(nexiaPlayer, damageSource);
        }

    }

    @Redirect(method = "doCloseContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;removed(Lnet/minecraft/world/entity/player/Player;)V"))
    private void removed(AbstractContainerMenu instance, Player player) {

        if (FfaUtil.isFfaPlayer(new NexiaPlayer((ServerPlayer) player))) {
            player.inventory.add(inventory.getCarried());
            player.inventory.setCarried(ItemStack.EMPTY);
            return;
        }

        player.containerMenu.removed(player);
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void detect(CallbackInfo ci){
        if(!DetectCommand.enabled) return;
        DetectCommand.detect(new NexiaPlayer((ServerPlayer) (Object) this));
    }
}
