package com.nexia.core.mixin.entity;

import com.nexia.ffa.FfaUtil;
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
        Entity entity = ((Entity) (Object) this);
        return FfaUtil.isFfaWorld(entity.level) ? Explosion.BlockInteraction.NONE : Explosion.BlockInteraction.BREAK;
    }
}
