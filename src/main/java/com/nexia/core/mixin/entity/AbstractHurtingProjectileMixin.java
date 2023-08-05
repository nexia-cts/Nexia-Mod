package com.nexia.core.mixin.entity;

import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin extends Entity {

    public AbstractHurtingProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // Slow down fireballs
    @Inject(method = "getInertia", cancellable = true, at = @At("HEAD"))
    private void getInertia(CallbackInfoReturnable<Float> cir) {
        if (BwAreas.isBedWarsWorld(level) && (Object)this instanceof LargeFireball) {
            cir.setReturnValue(BwUtil.getFireballInertia());
        }
    }

}
