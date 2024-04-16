package com.nexia.core.mixin.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    public final PlayerEntity player = PlayerEntity.class.cast(this);
    public PlayerEntityMixin() {
    }

    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.AFTER)
    )
    public void attack2(Entity target, CallbackInfo ci) {
        if (isPlayerSprintFixed.get(player.getUuid())) {
            double x = player.getVelocity().getX() / 0.6;
            double z = player.getVelocity().getZ() / 0.6;
            player.setVelocity(x, player.getVelocity().getY(), z);
        }
    }

    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER)
    )
    public void attack3(Entity target, CallbackInfo ci) {
        if (isPlayerSprintFixed.get(player.getUuid()))
            player.setSprinting(true);
    }
}
