package com.nexia.ffa.utilities;

import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.ffa.Main;
import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FfaAreas {
    public static ServerLevel ffaWorld = null;
    public static EntityPos spawn = new EntityPos(Main.config.spawnCoordinates[0], Main.config.spawnCoordinates[1], Main.config.spawnCoordinates[2], 0, 0);
    public static BlockPos spawnCorner1 = spawn.toBlockPos().offset(-9, -12, -9);
    public static BlockPos spawnCorner2 = spawn.toBlockPos().offset(9, 12, 9);

    public static BlockPos ffaCorner1 = spawn.toBlockPos().offset(-500, -spawn.y, -500);
    public static BlockPos ffaCorner2 = spawn.toBlockPos().offset(500, -spawn.y + 255, 500);

    public FfaAreas() {
    }

    public static boolean isFfaWorld(Level level) {
        return level.dimension().toString().contains(Main.config.worldName);
    }

    public static boolean isInFfaSpawn(Player player) {
        BlockVec3 pos = new BlockVec3(player.position());
        return pos.x >= spawnCorner1.getX() && pos.x <= spawnCorner2.getX() &&
                pos.y >= spawnCorner1.getY() && pos.y <= spawnCorner2.getY() &&
                pos.z >= spawnCorner1.getZ() && pos.z <= spawnCorner2.getZ();
    }

    public static void setFfaWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isFfaWorld(level)) {
                ffaWorld = level;
                break;
            }
        }
    }
}
