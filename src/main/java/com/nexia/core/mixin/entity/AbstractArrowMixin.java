package com.nexia.core.mixin.entity;

import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {

    @Unique
    private int arrowDamage;

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(D)I"))
    private int modifyArrowDamage(double vanillaCalculated) {

        ServerLevel level = (ServerLevel) ((AbstractArrow)(Object)this).level;

        if (BedwarsAreas.isBedWarsWorld(level)) {
            vanillaCalculated *= 0.625;
        }

        arrowDamage = (int)Math.ceil(vanillaCalculated);

        if(OitcGame.world.equals(level)) {
            arrowDamage = 1000;
        }

        return arrowDamage;
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(JJ)J"))
    private long modifyCritDamage(long vanillaCalculated, long intMaxValue) {

        ServerLevel level = (ServerLevel) ((AbstractArrow)(Object)this).level;

        if (BedwarsAreas.isBedWarsWorld(level)) {
            long critBoost = arrowDamage / 4 + 1;
            arrowDamage = (int)Math.min(arrowDamage + critBoost, Integer.MAX_VALUE);
        } else {
            arrowDamage = (int)Math.min(vanillaCalculated, intMaxValue);
        }

        if(OitcGame.world.equals(level)) {
            arrowDamage = 1000;
        }

        return arrowDamage;
    }
}
