package com.nexia.core.utilities.pos;

import net.minecraft.core.BlockPos;

public class PositionUtil {

    public static boolean isBetween(BlockPos corner1, BlockPos corner2, BlockPos between) {
        return corner1.getX() <= between.getX() && corner2.getX() >= between.getX() &&
                corner1.getY() <= between.getY() && corner2.getY() >= between.getY() &&
                corner1.getZ() <= between.getZ() && corner2.getZ() >= between.getZ();
    }



}
