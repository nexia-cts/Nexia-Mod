package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;

        if (FfaUtil.isFfaPlayer(player)) {
            FfaUtil.leaveOrDie(player, damageSource);

        } else if (BwAreas.isBedWarsWorld(getLevel())) {
            BwPlayerEvents.death(player);
        }
        else if(PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC){
            OitcGame.death(player, damageSource);
        }
        else if(PlayerDataManager.get(player).gameMode == PlayerGameMode.DUELS){
            DuelsGame.death(player, damageSource);
        }
    }

    /*
    @Inject(method = "die", at = @At("RETURN"))
    private void dieReturn(DamageSource damageSource, CallbackInfo ci){
        ServerPlayer player = (ServerPlayer)(Object)this;

        if(PlayerDataManager.get(player).gameMode == PlayerGameMode.DUELS){
            DuelsGame.death(player, damageSource);
        }
    }

     */

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
