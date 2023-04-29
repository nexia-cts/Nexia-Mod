package com.nexia.core.mixin.entity;

import com.nexia.core.Main;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(LivingEntity.class)
public abstract class AbstractLivingEntityMixin extends Entity {
    public AbstractLivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow public abstract double getAttributeValue(Attribute attribute);

    @Shadow public abstract ItemStack getBlockingItem();

    /**
     * @author Shinkume, NotCoded
     * @reason Modification of knockback.
     */
    @Overwrite
    public void knockback(float f, double d, double e) {
        double g = this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        ItemStack itemStack = this.getBlockingItem();
        if (!itemStack.isEmpty()) {
            g = Math.min(1.0, g + (double) ShieldItem.getShieldKnockbackResistanceValue(itemStack));
        }

        f = (float)((double)f * (1.0 - g));

        if ((!(f <= 0.0F)) && Main.config.enhancements.modifiedKnockback) {
            this.hasImpulse = true;
            Vec3 vec3 = this.getDeltaMovement();
            Vec3 vec32 = (new Vec3(d, 0.0, e)).normalize().scale((double)f);
            //this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround ? Math.min(0.4, (double)f * 0.75) : Math.min(0.4, vec3.y * 0.5 + (double)f * 0.675), vec3.z / 2.0 - vec32.z);
            this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround ? Math.min(0.4, (double)f * 0.75) : Math.min(0.4, vec3.y * 0.5 + (double)f * 0.625), vec3.z / 2.0 - vec32.z);
        } else if ((!(f <= 0.0F)) && !Main.config.enhancements.modifiedKnockback) {
            this.hasImpulse = true;
            Vec3 vec3 = this.getDeltaMovement();
            Vec3 vec32 = (new Vec3(d, 0.0, e)).normalize().scale((double)f);
            this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround ? Math.min(0.4, (double)f * 0.75) : Math.min(0.4, vec3.y + (double)f * 0.5), vec3.z / 2.0 - vec32.z);
        }
    }
}
