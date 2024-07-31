package com.nexia.core.mixin.misc;

import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.util.BedwarsUtil;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private @Nullable Entity source;

    @Shadow @Final private Level level;

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    protected Optional<Float> modifyExplosionResistance(ExplosionDamageCalculator instance, Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.isAir() && fluidState.isEmpty()) return Optional.empty();

        float resistance = blockState.getBlock().getExplosionResistance();
        if (BedwarsAreas.isBedWarsWorld(level)) {
            resistance = BedwarsUtil.modifyBlockExplosionRes(blockPos, blockState, source, resistance);
        }
        return Optional.of(Math.max(resistance, fluidState.getExplosionResistance()));
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;shouldBlockExplode(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;F)Z"))
    protected boolean shouldExplode(ExplosionDamageCalculator instance, Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float resistance) {
        if (BedwarsAreas.isBedWarsWorld(level)) {
            if (!BedwarsUtil.shouldExplode(blockPos, blockState)) return false;
        }
        return instance.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, resistance);
    }

    @ModifyArgs(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    protected void modifyKnockback(Args args) {
        if (BedwarsAreas.isBedWarsWorld(level)) {
            BedwarsUtil.modifyExplosionKb(source, args);
        }
    }

    @ModifyArg(method = "explode", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    protected float modifyDamage(float damage) {
        if (BedwarsAreas.isBedWarsWorld(level)) {
            return BedwarsUtil.getExplosionDamage(damage);
        }

        return damage;
    }

}