package com.nexia.core.mixin.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Shadow private int foodLevel;
    Player player;
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(Player player, CallbackInfo ci) {
        this.player = player;
    }

}
