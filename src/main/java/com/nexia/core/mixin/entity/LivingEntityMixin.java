package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {


    @Shadow public abstract double getAttributeValue(Attribute attribute);

    @Shadow public abstract ItemStack getBlockingItem();

    // Make void death instant
    @ModifyArg(method = "hurt", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    protected float hurt(DamageSource damageSource, float value) {
        if((Object) this instanceof ServerPlayer player){

            if(PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY) {
                return value;
            }
        }
        if (damageSource == DamageSource.OUT_OF_WORLD) return 1000000.0F;
        return value;
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
            if(instance instanceof Player) {
                g = Math.min(1.0, 1-(1-g)*(1-(double) ShieldItem.getShieldKnockbackResistanceValue(itemStack)));
            } else {
                g = Math.min(1.0, g + (double) ShieldItem.getShieldKnockbackResistanceValue(itemStack));
            }
        }

        f = (float)((double)f * (1.0 - g));
        if (!(f <= 0.0F)) {
            instance.hasImpulse = true;
            Vec3 vec3 = instance.getDeltaMovement();
            Vec3 vec32 = (new Vec3(d, 0.0, e)).normalize().scale(f);
            instance.setDeltaMovement(vec3.x / 2.0 - vec32.x, instance.isOnGround() ? Math.min(0.4, (double)f * 0.75) : Math.min(0.4, vec3.y + (double)f * 0.5), vec3.z / 2.0 - vec32.z);
        }
    }

    @Redirect(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    private float redirectArmorProtCalculation(float damage, float protection) {
        if ((Object) this instanceof ServerPlayer player) {

            if (BwAreas.isBedWarsWorld(player.getLevel())) {
                return BwUtil.playerProtCalculation(damage, protection);
            }

        }
        return CombatRules.getDamageAfterMagicAbsorb(damage, protection);
    }

}
