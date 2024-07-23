package com.nexia.core.mixin.item;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.base.player.NexiaPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

            if(serverPlayer.level.equals(LobbyUtil.lobbyWorld) && !serverPlayer.isCreative()) {
                cir.setReturnValue(itemStack);
                nexiaPlayer.refreshInventory();
            }
        }
    }
}
