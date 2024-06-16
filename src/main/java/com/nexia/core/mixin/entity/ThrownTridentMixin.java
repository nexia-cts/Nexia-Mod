package com.nexia.core.mixin.entity;

import com.nexia.core.Main;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)F"))
    private float getDamageBonus(ItemStack itemStack, LivingEntity livingEntity) {
        float bonus = EnchantmentHelper.getDamageBonus(itemStack, livingEntity);

        /*
        if ((Object)this instanceof BwTrident) {
            bonus -= 1;
        }
        */

        bonus -= 2;

        return bonus;
    }

}
