package com.nexia.core.mixin.entity;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {

    private int arrowDamage;

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(D)I"))
    private int modifyArrowDamage(double vanillaCalculated) {
        arrowDamage = (int)Math.ceil(vanillaCalculated);
        return arrowDamage;
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(JJ)J"))
    private long modifyCritDamage(long vanillaCalculated, long intMaxValue) {
        arrowDamage = (int)Math.min(vanillaCalculated, intMaxValue);
        return arrowDamage;
    }

}
