package com.nexia.core.utilities.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class BlockUtil {

    public static void placeBed(Level level, BedBlock block, BlockPos blockPos, float headRotation) {

        int intRotation = (Math.round(headRotation / 90) * 90) % 360;
        if (intRotation <= -180) intRotation += 360;
        if (intRotation > 180) intRotation -= 360;
        Rotation rotation;
        Direction direction;

        switch (intRotation) {
            case 90 -> {
                rotation = Rotation.CLOCKWISE_90;
                direction = Direction.WEST;
            }
            case 180 -> {
                rotation = Rotation.CLOCKWISE_180;
                direction = Direction.NORTH;
            }
            case -90 -> {
                rotation = Rotation.COUNTERCLOCKWISE_90;
                direction = Direction.EAST;
            }
            default -> {
                rotation = Rotation.NONE;
                direction = Direction.SOUTH;
            }
        }

        BlockState blockState = block.defaultBlockState();
        blockState = blockState.setValue(BedBlock.FACING, rotation.rotate(blockState.getValue(BedBlock.FACING)));
        blockState = blockState.setValue(BedBlock.PART, BedPart.HEAD);
        level.setBlock(blockPos, blockState, 3);

        blockState = blockState.setValue(BedBlock.PART, BedPart.FOOT);
        level.setBlock(blockPos.relative(direction), blockState, 3);
    }

    public static String blockToText(BlockState blockState) {
        return blockToText(blockState.getBlock());
    }

    public static String blockToText(Block block) {
        String[] splitRegistryKey = Registry.BLOCK.getKey(block).toString().split(":");
        if (splitRegistryKey.length < 2) return splitRegistryKey[0];
        return splitRegistryKey[1];
    }

}
