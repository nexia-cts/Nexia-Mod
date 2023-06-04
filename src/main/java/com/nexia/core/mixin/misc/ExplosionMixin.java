package com.nexia.core.mixin.misc;

import com.nexia.core.Main;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
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
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private @Nullable Entity source;

    @Shadow @Final private Level level;

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    private Optional<Float> modifyExplosionResistance(ExplosionDamageCalculator instance, Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.isAir() && fluidState.isEmpty()) return Optional.empty();

        float resistance = blockState.getBlock().getExplosionResistance();
        if (BwAreas.isBedWarsWorld(level)) {
            resistance = BwUtil.modifyBlockExplosionRes(blockPos, blockState, source, resistance);
        }
        return Optional.of(Math.max(resistance, fluidState.getExplosionResistance()));
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;shouldBlockExplode(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;F)Z"))
    private boolean shouldExplode(ExplosionDamageCalculator instance, Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float resistance) {

        /*
        if (BwAreas.isBedWarsWorld(level)) {
            if (!BwUtil.shouldExplode(blockPos, blockState)) return false;
        }

        return instance.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, resistance);

         */

        //return (!BwAreas.isBedWarsWorld(level) || BwUtil.shouldExplode(blockPos, blockState)) && instance.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, resistance);
        return BwAreas.isBedWarsWorld(level) && !BwUtil.shouldExplode(blockPos, blockState) ? false : instance.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, resistance);
    }

    @ModifyArgs(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private void modifyKnockback(Args args) {

        if (BwAreas.isBedWarsWorld(level)) {
            BwUtil.modifyExplosionKb(source, args);
        } else if(Main.config.enhancements.modifiedKnockback){
            args.set(1, (double)args.get(1)*100);
            args.set(2, (double)args.get(2)*100);
            args.set(3, (double)args.get(3)*100);
        }
    }

    @ModifyArg(method = "explode", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float modifyDamage(float damage) {

        /*
        if (BwAreas.isBedWarsWorld(level)) {
            return BwUtil.getExplosionDamage(damage);
        }

        return damage;

         */

        return BwAreas.isBedWarsWorld(level) ? BwUtil.getExplosionDamage(damage) : damage;
    }

}