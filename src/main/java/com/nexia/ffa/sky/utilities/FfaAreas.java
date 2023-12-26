package com.nexia.ffa.sky.utilities;

import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.pos.*;
import com.nexia.ffa.Main;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public class FfaAreas {
    public static ServerLevel ffaWorld = null;
    public static EntityPos spawn = new EntityPos(Main.sky.spawnCoordinates[0], Main.sky.spawnCoordinates[1], Main.sky.spawnCoordinates[2], 0, 0);
    public static BlockPos spawnCorner1 = spawn.toBlockPos().offset(-12, -12, -12);
    public static BlockPos spawnCorner2 = spawn.toBlockPos().offset(12, 12, 12);

    public static BlockPos ffaCorner1 = spawn.toBlockPos().offset(99, -spawn.y, 132);
    public static BlockPos ffaCorner2 = spawn.toBlockPos().offset(-100, spawn.y + 255, -98);

    public FfaAreas() {
    }

    public static boolean isFfaWorld(Level level) {
        return level.dimension().toString().contains(Main.sky.worldName);
    }

    public static boolean isInFfaSpawn(ServerPlayer player) {
        return PositionUtil.isBetween(spawnCorner1, spawnCorner2, player.blockPosition());
    }

    public static void setFfaWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isFfaWorld(level)) {
                ffaWorld = level;
                break;
            }
        }
    }

    public static float getVoidY() {
        int defaultY = ffaCorner1.getY() - 8;

        AABB aabb = new AABB(ffaCorner1.offset(-10, -64, -10), ffaCorner2.offset(10, 64, 10));
        Predicate<ThrownEnderpearl> predicate = o -> true;
        if (!ffaWorld.getEntities(EntityType.ENDER_PEARL, aabb, predicate).isEmpty()) {
            return defaultY - 48;
        }
        return defaultY;
    }

    public static boolean canBuild(ServerPlayer player, BlockPos blockPos) {
        if (protectionMap == null) {
            player.sendMessage(LegacyChatFormat.formatFail("Something went wrong, please inform the developers."), Util.NIL_UUID);
            return false;
        }

        return protectionMap.canBuiltAt(ffaCorner1, blockPos, player, true);
    }

    private static final String protMapPath = FfaSkyUtil.ffaSkyDir + "/protectionMap.json";

    private static final ProtectionBlock[] protMapBlocks =  {
            new ProtectionBlock(Blocks.AIR, true, null)
    };
    private static final ProtectionBlock notListedBlock =
            new ProtectionBlock(null, false, "You can't build here.");
    private static final String outsideMessage = "You can't build here.";

    public static ProtectionMap protectionMap = ProtectionMap.importMap(
            protMapPath, protMapBlocks, notListedBlock, outsideMessage);

    public static void createProtectionMap(ServerPlayer player) {
        protectionMap = new ProtectionMap(player,
                ffaCorner1, ffaCorner2, protMapPath, protMapBlocks, notListedBlock, outsideMessage);
    }

}
