package com.nexia.ffa.sky;

import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.ffa.sky.utilities.FfaAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

public class SkyFfaBlocks {

    private static class SkyFfaBlock {
        public BlockPos blockPos;
        public int ticks;

        public SkyFfaBlock(BlockPos blockPos, int ticks) {
            this.blockPos = blockPos;
            this.ticks = ticks;
        }
    }

    private static int ticks = 0;

    private static final Queue<SkyFfaBlock> playerPlacedBlocks = new LinkedList<>();
    private static final int placedBlockTime = 10 * 20;
    private static final Queue<SkyFfaBlock> disappearingWool = new LinkedList<>();
    private static final int disappearingBlockTime = 2 * 20;

    private static final Block disappearingWoolType = Blocks.GRAY_WOOL;

    public static void tick() {
        ticks++;

        while (!playerPlacedBlocks.isEmpty()) {
            SkyFfaBlock firstBlock = playerPlacedBlocks.peek();
            if (firstBlock.ticks > ticks) break;

            playerPlacedBlocks.remove();
            setDisappearingBlock(firstBlock.blockPos);
        }

        while (!disappearingWool.isEmpty()) {
            SkyFfaBlock firstBlock = disappearingWool.peek();
            if (firstBlock.ticks > ticks) break;

            disappearingWool.remove();
            blockDisappear(firstBlock.blockPos);
        }

    }

    public static void clearAllBlocks() {
        while (!playerPlacedBlocks.isEmpty()) {
            SkyFfaBlock firstBlock = playerPlacedBlocks.peek();

            playerPlacedBlocks.remove();
            setDisappearingBlock(firstBlock.blockPos);
        }

        while (!disappearingWool.isEmpty()) {
            SkyFfaBlock firstBlock = disappearingWool.peek();

            disappearingWool.remove();
            blockDisappear(firstBlock.blockPos);
        }
    }

    public static void placeBlock(BlockPos blockPos) {
        playerPlacedBlocks.add(new SkyFfaBlock(blockPos, ticks + placedBlockTime));
    }

    private static boolean contains(Queue<SkyFfaBlock> blocks, BlockPos blockPos) {
        for (SkyFfaBlock block : blocks) {
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

        disappearingWool.add(new SkyFfaBlock(blockPos, ticks + disappearingBlockTime));
        if (BlockUtil.blockToText(blockState).endsWith("_wool")) {
            world.setBlock(blockPos, disappearingWoolType.defaultBlockState(), 3);
        }
    }

    private static void blockDisappear(BlockPos blockPos) {
        if (contains(playerPlacedBlocks, blockPos) || contains(disappearingWool, blockPos)) return;
        FfaAreas.ffaWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
    }

}