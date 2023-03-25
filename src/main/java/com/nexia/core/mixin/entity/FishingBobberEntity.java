package com.nexia.core.mixin.entity;

import com.nexia.core.Main;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingBobberEntity extends Entity {


    public FishingBobberEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow private Entity hookedIn;

    @Shadow @Nullable public abstract Player getPlayerOwner();

    /**
     * @author NotCoded, Shinkume
     * @reason Make fishing rod hook pull towards the player.
     */
    @Overwrite
    public void bringInHookedEntity() {
        Entity entity = this.getPlayerOwner();
        if (entity != null) {
            Vec3 vec3;
            vec3 = (new Vec3(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ())).scale(0.1);
            if(Main.config.modifiedRods) {
                vec3 = (new Vec3(entity.getX() - this.getX()/2, entity.getY() - this.getY()/2, entity.getZ() - this.getZ()/2)).scale(0.2);
            }
            this.hookedIn.setDeltaMovement(this.hookedIn.getDeltaMovement().add(vec3));
        }
    }

}