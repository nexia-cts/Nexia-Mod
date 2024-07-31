package com.nexia.core.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.util.BedwarsUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {


    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract double getAttributeValue(Attribute attribute);

    @Shadow public abstract ItemStack getBlockingItem();

    // Make void death instant
    @WrapOperation(method = "outOfWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    protected boolean killInVoid(LivingEntity instance, DamageSource damageSource, float f, Operation<Boolean> original) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            if (((CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(serverPlayer.getUUID())).gameMode == PlayerGameMode.LOBBY) {
                return original.call(instance, damageSource, f);
            }
        }
        return original.call(instance, damageSource, Float.MAX_VALUE);
    }


    /**
     * @author NotCoded
     * @reason Fix Shield Knockback
     */
    @Overwrite
    public void knockback(float f, double d, double e) {
        LivingEntity instance = (LivingEntity) (Object) this;
        double g = this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        ItemStack itemStack = this.getBlockingItem();
        if (!itemStack.isEmpty()) {
            if (instance instanceof Player) g = Math.min(1.0, 1-(1-g)*(1-(double) ShieldItem.getShieldKnockbackResistanceValue(itemStack)));
            else g = Math.min(1.0, g + (double) ShieldItem.getShieldKnockbackResistanceValue(itemStack));
        }

        f = (float)((double)f * (1.0 - g));
        if (!(f <= 0.0F)) {
            instance.hasImpulse = true;
            Vec3 vec3 = instance.getDeltaMovement();
            Vec3 vec32 = (new Vec3(d, 0.0, e)).normalize().scale(f);
            instance.setDeltaMovement(vec3.x / 2.0 - vec32.x, instance.isOnGround() ? Math.min(0.4, (double)f * 0.75) : Math.min(0.4, vec3.y + (double)f * 0.5),vec3.z / 2.5 - vec32.z);
        }
    }

    @Redirect(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    private float redirectArmorProtCalculation(float damage, float protection) {
        if ((Object) this instanceof ServerPlayer player) {

            if (BedwarsAreas.isBedWarsWorld(player.getLevel())) {
                return BedwarsUtil.playerProtCalculation(damage, protection);
            }

        }
        return CombatRules.getDamageAfterMagicAbsorb(damage, protection);
    }

}
