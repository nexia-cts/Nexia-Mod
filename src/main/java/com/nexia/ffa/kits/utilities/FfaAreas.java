package com.nexia.ffa.kits.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.pos.PositionUtil;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.util.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class FfaAreas {
    public static ServerLevel ffaWorld = null;
    public static World nexusFfaWorld = null;
    public static Location nexusFfaLocation = null;

    public static EntityPos spawn = new EntityPos(0.5, 128, 0.5, 0, 0);

    public static BlockPos spawnCorner1 = spawn.toBlockPos().offset(-9, -12, -9);
    public static BlockPos spawnCorner2 = spawn.toBlockPos().offset(9, 12, 9);

    public static BlockPos ffaCorner1 = spawn.toBlockPos().offset(-500, -spawn.y, -500);
    public static BlockPos ffaCorner2 = spawn.toBlockPos().offset(500, -spawn.y + 255, 500);

    public static boolean isFfaWorld(Level level) {
        return level.dimension().location().toString().equals("ffa:kits");
    }

    public static boolean isInFfaSpawn(NexiaPlayer player) {
        return PositionUtil.isBetween(spawnCorner1, spawnCorner2, player.getLocation());
    }

    public static void setFfaWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isFfaWorld(level)) {
                ffaWorld = level;
                nexusFfaWorld = WorldUtil.getWorld(level);
                break;
            }
        }

        nexusFfaLocation = new Location(spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch, nexusFfaWorld);
    }
}
