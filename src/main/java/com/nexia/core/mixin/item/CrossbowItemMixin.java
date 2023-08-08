package com.nexia.core.mixin.item;

import com.nexia.core.utilities.item.ItemStackUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
    public void preventKitFFAplayers(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if(livingEntity instanceof Player player){
            if(com.nexia.ffa.kits.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(player)) {
                ci.cancel();
                ItemStackUtil.sendInventoryRefreshPacket((ServerPlayer) player);
            }
        }
    }
}
