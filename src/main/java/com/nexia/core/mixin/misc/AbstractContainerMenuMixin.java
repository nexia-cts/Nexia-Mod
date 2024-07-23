package com.nexia.core.mixin.misc;

import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.base.player.NexiaPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Inject(method = "removed", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void removed(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

        if (!EventUtil.dropItem(nexiaPlayer, player.inventory.getCarried())) {
            if (player.inventory.add(player.inventory.getCarried())) {
                player.inventory.setCarried(ItemStack.EMPTY);
            }
            ci.cancel();
        }
    }

}
