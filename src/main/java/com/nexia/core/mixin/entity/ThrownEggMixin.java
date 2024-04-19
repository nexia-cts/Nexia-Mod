package com.nexia.core.mixin.entity;

import com.nexia.minigames.games.bedwars.custom.BwBridgeEgg;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ThrownEgg.class)
public class ThrownEggMixin {

    @Redirect(method = "onHit", at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/util/Random;nextInt(I)I"))
    private int modifyChickenSpawnOdds(Random random, int bound) {

        // Spawns chicken if return value is 0
        if ((Object) this instanceof BwBridgeEgg) {
            return 1;
        }
        return random.nextInt(bound);
    }

}
