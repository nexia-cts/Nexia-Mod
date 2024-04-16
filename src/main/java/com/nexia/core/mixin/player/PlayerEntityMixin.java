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

    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V", shift = At.Shift.AFTER)
    )
    public void attack3(Entity target, CallbackInfo ci) {
        boolean found = false;
        for (String tag : player.getTags()) {
            if (tag == "sprintfixdisable") {
                found = true;
                break;
            }
        }

        if (!found) {
            player.setSprinting(true);
        }
    }
}
