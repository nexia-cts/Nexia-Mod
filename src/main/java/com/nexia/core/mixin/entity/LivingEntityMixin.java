package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

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
