package com.nexia.core.utilities.pos;

import com.nexia.nexus.api.world.util.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class PositionUtil {

    public static boolean isBetween(BlockPos corner1, BlockPos corner2, BlockPos between) {
        return corner1.getX() <= between.getX() && corner2.getX() >= between.getX() &&
                corner1.getY() <= between.getY() && corner2.getY() >= between.getY() &&
                corner1.getZ() <= between.getZ() && corner2.getZ() >= between.getZ();
    }

    public static boolean isBetween(BlockPos corner1, BlockPos corner2, Location between) {
        return corner1.getX() <= between.getX() && corner2.getX() >= between.getX() &&
                corner1.getY() <= between.getY() && corner2.getY() >= between.getY() &&
                corner1.getZ() <= between.getZ() && corner2.getZ() >= between.getZ();
    }


    public static boolean isBetween(AABB corners, BlockPos between) {
        return corners.minX <= between.getX() && corners.minX >= between.getX() &&
                corners.minY <= between.getY() && corners.minY >= between.getY() &&
                corners.minZ <= between.getZ() && corners.minZ >= between.getZ();
    }

    public static boolean isBetween(AABB corners, Location between) {
        return corners.minX <= between.getX() && corners.minX >= between.getX() &&
                corners.minY <= between.getY() && corners.minY >= between.getY() &&
                corners.minZ <= between.getZ() && corners.minZ >= between.getZ();
    }
}
