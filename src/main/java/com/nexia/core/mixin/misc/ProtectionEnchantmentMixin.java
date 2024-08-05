package com.nexia.core.mixin.misc;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ProtectionEnchantment.class)
public class ProtectionEnchantmentMixin {
    /**
     * @author NotInfinityy
     * @reason This fixes blast protection not reducing explosion knockback
     * <a href="https://bugs.mojang.com/browse/MC-198809">MC-198809</a>
     */
    @Overwrite
    public static double getExplosionKnockbackAfterDampener(LivingEntity livingEntity, double d) {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, livingEntity);
        if (i > 0) {
            d *= Mth.clamp(1.0 - (double)i * 0.15, 0.0, 1.0);
        }

        return d;
    }
}
