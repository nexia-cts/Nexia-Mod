package com.nexia.core.mixin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {

    public ProjectileMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract void shoot(double d, double e, double f, float g, float h);

    /**
     * @author NotCoded
     * @reason Port 1.16.4 projectile code
     */
    @Overwrite
    public void shootFromRotation(Entity entity, float f, float g, float h, float i, float j) {
        float k = -Mth.sin(g * 0.017453292F) * Mth.cos(f * 0.017453292F);
        float l = -Mth.sin((f + h) * 0.017453292F);
        float m = Mth.cos(g * 0.017453292F) * Mth.cos(f * 0.017453292F);
        this.shoot((double)k, (double)l, (double)m, i, j);
        Vec3 vec3 = entity.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.isOnGround() ? 0.0 : vec3.y, vec3.z));
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return null;
    }
}
