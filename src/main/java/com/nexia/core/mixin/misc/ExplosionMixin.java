package com.nexia.core.mixin.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private @Nullable Entity source;

    @Shadow @Final private Level level;

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    protected Optional<Float> modifyExplosionResistance(ExplosionDamageCalculator instance, Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.isAir() && fluidState.isEmpty()) return Optional.empty();

        float resistance = blockState.getBlock().getExplosionResistance();
        return Optional.of(Math.max(resistance, fluidState.getExplosionResistance()));
    }

}