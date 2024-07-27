package com.nexia.core.mixin.item;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.pot.utilities.FfaPotUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Shadow public abstract boolean isEdible();

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void cancelEatingInPotSpawn(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (isEdible() && player instanceof ServerPlayer serverPlayer) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);
            if (FfaPotUtil.INSTANCE.isFfaPlayer(nexiaPlayer) && FfaPotUtil.INSTANCE.wasInSpawn.contains(player.getUUID())) {
                cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(interactionHand)));
                nexiaPlayer.refreshInventory();
            }
        }
    }
}
