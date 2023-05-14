package com.nexia.core.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LargeFireball.class)
public class LargeFireballMixin extends Fireball {

    public LargeFireballMixin(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    // Set fireball entity to fireball
    @ModifyArg(method = "onHit", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;"))
    protected Entity onHitSetEntity(@Nullable Entity entity) {
        return this;
    }

    // Remove fire from fireballs
    @ModifyArg(method = "onHit", index = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;"))
    protected boolean onHitRemoveFire(boolean doFire) {
        return false;
    }

    // Make block destruction mobGriefing independent
    @ModifyArg(method = "onHit", index = 6, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;"))
    protected Explosion.BlockInteraction onHitSetBlockInteraction(Explosion.BlockInteraction blockInteraction) {
        return Explosion.BlockInteraction.DESTROY;
    }

}
