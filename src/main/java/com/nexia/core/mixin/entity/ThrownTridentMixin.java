package com.nexia.core.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author NotCoded
     * @reason Remove trident despawning.
     */
    @Overwrite
    public void tickDespawn(){
        if(this.pickup != Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }
}
