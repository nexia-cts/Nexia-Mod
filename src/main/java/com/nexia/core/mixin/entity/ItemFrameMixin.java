package com.nexia.core.mixin.entity;

import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends Entity {

    public ItemFrameMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interact", cancellable = true, at = @At("HEAD"))
    private void canTakeItem(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {

        // Disable interacting with armor stands in ffa
        if (FfaUtil.isFfaPlayer(player) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

    }

    @Inject(method = "removeFramedMap", cancellable = true, at = @At("HEAD"))
    private void canTakeItem(ItemStack itemStack, CallbackInfo ci) {

        // Disable interacting with item frames in ffa
        if ((FfaAreas.isFfaWorld(level) || com.nexia.ffa.kits.utilities.FfaAreas.isFfaWorld(level) || com.nexia.ffa.uhc.utilities.FfaAreas.isFfaWorld(level) || com.nexia.ffa.pot.utilities.FfaAreas.isFfaWorld(level)) && !getTags().contains("removeFrameMap")) {
            ci.cancel();
            return;
        }

    }
}
