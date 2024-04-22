package com.nexia.core.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin {
    @Shadow @Nullable public abstract MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos);

    @Shadow protected abstract Stat<ResourceLocation> getOpenChestStat();

    /**
     * @author infinityy
     * @reason figure out a better way to do this lol
     */
    @Overwrite()
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        CompoundTag compoundTag = new CompoundTag();

        level.getBlockEntity(blockPos).save(compoundTag);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            MenuProvider menuProvider = this.getMenuProvider(blockState, level, blockPos);
            if (menuProvider != null) {
                player.openMenu(menuProvider);
                player.awardStat(this.getOpenChestStat());
                PiglinAi.angerNearbyPiglins(player, true);
            }

            level.getBlockEntity(blockPos).load(blockState, compoundTag);
            return InteractionResult.CONSUME;
        }
    }
}
