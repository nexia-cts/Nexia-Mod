package com.nexia.ffa.classic.utilities;

import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.ffa.FfaAreas;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.util.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ClassicFfaAreas implements FfaAreas {
    public static ServerLevel ffaWorld = null;
    public static World nexusFfaWorld = null;
    public static Location nexusFfaLocation = null;

    public static EntityPos spawn = new EntityPos(0.5, 80, 0.5, 0, 0);
    public static BlockPos spawnCorner1 = spawn.toBlockPos().offset(-14, -6, -14);
    public static BlockPos spawnCorner2 = spawn.toBlockPos().offset(14, 9, 14);

    public static BlockPos ffaCorner1 = spawn.toBlockPos().offset(-150, -spawn.y, -159);
    public static BlockPos ffaCorner2 = spawn.toBlockPos().offset(150, -spawn.y + 255, 150);

    public boolean isFfaWorld(Level level) {
        return isFfaWorldStatic(level);
    }

    public static boolean isFfaWorldStatic(Level level) {
        return level.dimension().location().toString().equals("ffa:classic");
    }

    @Override
    public ServerLevel getFfaWorld() {
        return ffaWorld;
    }

    @Override
    public World getNexusFfaWorld() {
        return nexusFfaWorld;
    }

    @Override
    public Location getFfaLocation() {
        return nexusFfaLocation;
    }

    @Override
    public EntityPos getSpawn() {
        return spawn;
    }

    @Override
    public AABB getSpawnCorners() {
        return new AABB(spawnCorner1, spawnCorner2);
    }

    @Override
    public AABB getFfaCorners() {
        return new AABB(ffaCorner1, ffaCorner2);
    }

    public static void setFfaWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isFfaWorldStatic(level)) {
                ffaWorld = level;
                nexusFfaWorld = WorldUtil.getWorld(level);
                break;
            }
        }

        nexusFfaLocation = new Location(spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch, nexusFfaWorld);
    }
}
