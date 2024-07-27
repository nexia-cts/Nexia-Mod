package com.nexia.core.mixin.item;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.pot.utilities.FfaPotUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {CrossbowItem.class, BowItem.class})
public class BowItemMixins {
    @Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
    public void preventKitFFAplayers(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if(livingEntity instanceof ServerPlayer player){
            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

            if(((FfaPotUtil.INSTANCE.isFfaPlayer(nexiaPlayer) && FfaPotUtil.INSTANCE.wasInSpawn.contains(player.getUUID())) ||
                    (FfaKitsUtil.INSTANCE.isFfaWorld(player.level) && FfaKitsUtil.INSTANCE.isInFfaSpawn(nexiaPlayer)) ||
                    (FfaUhcUtil.INSTANCE.isFfaWorld(player.level) && FfaUhcUtil.INSTANCE.isInFfaSpawn(nexiaPlayer)) ||
                    (FfaSkyUtil.INSTANCE.isFfaWorld(player.level) && FfaSkyUtil.INSTANCE.isInFfaSpawn(nexiaPlayer)) ||
                    (player.getLevel().equals(LobbyUtil.lobbyWorld))
            ) && !player.isCreative()) {
                ci.cancel();
                nexiaPlayer.refreshInventory();
            }
        }
    }
}
