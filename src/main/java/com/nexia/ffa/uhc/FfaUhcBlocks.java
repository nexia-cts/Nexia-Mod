package com.nexia.ffa.uhc;

import com.nexia.ffa.uhc.utilities.FfaAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.LinkedList;
import java.util.Queue;

public class FfaUhcBlocks {
    private static class FfaUhcBlock {
        public BlockPos blockPos;
        public int ticks;

        public boolean isFluid;

        public FfaUhcBlock(BlockPos blockPos, int ticks, boolean isFluid) {
            this.blockPos = blockPos;
            this.ticks = ticks;
            this.isFluid = isFluid;
        }
    }

    private static int ticks = 0;

    private static final Queue<FfaUhcBlock> playerPlacedBlocks = new LinkedList<>();
    private static final int placedBlockTime = 30 * 20;
    private static final Queue<FfaUhcBlock> disappearingBlocks = new LinkedList<>();
    private static final int disappearingBlockTime = 6 * 20;

    public static void tick() {
        ticks++;

        while (!playerPlacedBlocks.isEmpty()) {
            FfaUhcBlock firstBlock = playerPlacedBlocks.peek();
            if (firstBlock.ticks > ticks) break;

            playerPlacedBlocks.remove();
            setDisappearingBlock(firstBlock.blockPos);
        }

        while (!disappearingBlocks.isEmpty()) {
            FfaUhcBlock firstBlock = disappearingBlocks.peek();
            if (firstBlock.ticks > ticks) break;

            disappearingBlocks.remove();
            blockDisappear(firstBlock.blockPos);
        }

    }

    public static void placeBlock(BlockPos blockPos, boolean isFluid) {

        if(isFluid) {
            blockPos = new BlockPos(
                    blockPos.getX(),
                    blockPos.getY() + 1,
                    blockPos.getZ()
            );
        }

        playerPlacedBlocks.add(new FfaUhcBlock(blockPos, ticks + placedBlockTime, isFluid));
    }

    private static boolean contains(Queue<FfaUhcBlock> blocks, BlockPos blockPos) {
        for (FfaUhcBlock block : blocks) {
            if (block.blockPos.equals(blockPos)) {
                return true;
            }
        }
        return false;
    }

    private static void setDisappearingBlock(BlockPos blockPos) {
        if (contains(playerPlacedBlocks, blockPos)) return;

        ServerLevel world = FfaAreas.ffaWorld;
        BlockState blockState = world.getBlockState(blockPos);

        boolean isFluid = blockState.getBlock() != null && blockState.getBlock() == Blocks.WATER && world.getFluidState(blockPos).isSource();

        disappearingBlocks.add(new FfaUhcBlock(blockPos, ticks + disappearingBlockTime, isFluid));
        world.setBlock(blockPos, blockState, 3);

    }

    private static void blockDisappear(BlockPos blockPos) {
        if (contains(playerPlacedBlocks, blockPos) || contains(disappearingBlocks, blockPos)) return;
        FfaAreas.ffaWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
    }
}
