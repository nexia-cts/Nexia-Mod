package com.nexia.core.mixin.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {
    public final Player player = Player.class.cast(this);
    public PlayerEntityMixin() {
    }

    /*@Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.AFTER)
    )
    public void attack2(Entity target, CallbackInfo ci) {
        if (isPlayerSprintFixed.get(player.getUuid())) {
            double x = player.getVelocity().getX() / 0.6;
            double z = player.getVelocity().getZ() / 0.6;
            player.setVelocity(x, player.getVelocity().getY(), z);
        }
    }*/

    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V", shift = At.Shift.AFTER)
    )
    public void attack3(Entity target, CallbackInfo ci) {
        for (String tag : player.getTags()) {
            if (tag == "sprintfixdisable") {
                return;
            }
        }
        player.setSprinting(true);
    }
}
