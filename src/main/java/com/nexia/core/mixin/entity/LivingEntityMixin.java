package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // Make void death instant
    @ModifyArg(method = "hurt", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    protected float hurt(DamageSource damageSource, float value) {
        if((Object)this instanceof ServerPlayer){
            ServerPlayer player = (ServerPlayer)(Object)this;

            if(PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY) {
                return value;
            }
        }
        if (damageSource == DamageSource.OUT_OF_WORLD) return 1000000.0F;
        return value;
    }

}
