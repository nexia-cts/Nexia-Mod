package com.nexia.core.mixin.entity;

import com.nexia.ffa.utilities.FfaAreas;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends Entity {

    public ItemFrameMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interact", cancellable = true, at = @At("HEAD"))
    private void canTakeItem(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {

        // Disable interacting with armor stands in ffa
        if (FfaAreas.isFfaWorld(level) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

    }
}
