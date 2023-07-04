package com.nexia.core.mixin.entity;

import com.nexia.minigames.games.bedwars.areas.BwAreas;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {

    private int arrowDamage;

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(D)I"))
    private int modifyArrowDamage(double vanillaCalculated) {

        if (BwAreas.isBedWarsWorld(((AbstractArrow)(Object)this).level)) {
            vanillaCalculated *= 0.625;
        }

        arrowDamage = (int)Math.ceil(vanillaCalculated);
        return arrowDamage;
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(JJ)J"))
    private long modifyCritDamage(long vanillaCalculated, long intMaxValue) {

        if (BwAreas.isBedWarsWorld(((AbstractArrow)(Object)this).level)) {
            long critBoost = arrowDamage / 4 + 1;
            arrowDamage = (int)Math.min(arrowDamage + critBoost, Integer.MAX_VALUE);

        } else {
            arrowDamage = (int)Math.min(vanillaCalculated, intMaxValue);
        }

        return arrowDamage;
    }
}
