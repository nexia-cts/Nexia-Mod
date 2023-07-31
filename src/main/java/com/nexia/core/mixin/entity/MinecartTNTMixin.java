package com.nexia.core.mixin.entity;

import com.nexia.ffa.utilities.FfaAreas;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartTNT.class)
public class MinecartTNTMixin {
    @Redirect(method = "explode", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Explosion$BlockInteraction;BREAK:Lnet/minecraft/world/level/Explosion$BlockInteraction;"))
    public Explosion.BlockInteraction interact() {
        return ((Entity) (Object) this).level == FfaAreas.ffaWorld ? Explosion.BlockInteraction.NONE : Explosion.BlockInteraction.BREAK;
    }
}