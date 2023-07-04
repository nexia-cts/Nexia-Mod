package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.DuelGUI;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "attack", at = @At("HEAD"))
    public void onAttack(Entity entity, CallbackInfo ci) {
        ServerPlayer attacker = (ServerPlayer) (Object) this;
        if(level == LobbyUtil.lobbyWorld && entity instanceof ServerPlayer player && player != attacker &&
                this.getItemInHand(InteractionHand.MAIN_HAND).getDisplayName().toString().toLowerCase().contains("queue sword")) {
            DuelGUI.openDuelGui(attacker, player);
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
}
