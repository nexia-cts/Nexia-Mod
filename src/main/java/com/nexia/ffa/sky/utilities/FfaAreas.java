package com.nexia.ffa.sky.utilities;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.pos.PositionUtil;
import com.nexia.core.utilities.pos.ProtectionBlock;
import com.nexia.core.utilities.pos.ProtectionMap;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.util.Location;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class FfaAreas {
    public static ServerLevel ffaWorld = null;
    public static World nexusFfaWorld = null;
    public static Location nexusFfaLocation = null;

    public static EntityPos spawn = new EntityPos(0.5, 80, 0.5, 0, 0);

    private static final int mapRadius = 30;

    public static final int buildLimitY = 80;
    public static BlockPos spawnCorner1 = spawn.toBlockPos().offset(-6, -5, -6);
    public static BlockPos spawnCorner2 = spawn.toBlockPos().offset(6, 5, 6);

    public static BlockPos ffaCorner1 = spawn.toBlockPos().offset(-mapRadius, -spawn.y, -mapRadius);
    public static BlockPos ffaCorner2 = spawn.toBlockPos().offset(mapRadius, spawn.y + 255, mapRadius);

    public FfaAreas() {
    }

    public static boolean isFfaWorld(Level level) {
        return level.dimension().location().toString().equals("ffa:sky");
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

    public static float getVoidY() {
        int defaultY = 90;

        /*
        AABB aabb = new AABB(ffaCorner1.offset(-10, -64, -10), ffaCorner2.offset(10, 64, 10));
        Predicate<ThrownEnderpearl> predicate = o -> true;
        if (!ffaWorld.getEntities(EntityType.ENDER_PEARL, aabb, predicate).isEmpty()) {
            return defaultY - 48;
        }
         */

        // fuck that nerd math


        return defaultY;
    }

    public static boolean canBuild(NexiaPlayer player, BlockPos blockPos) {
        if (protectionMap == null) {
            player.sendMessage(Component.text("Something went wrong, please inform the admins").color(ChatFormat.failColor));
            return false;
        }

        return protectionMap.canBuiltAt(ffaCorner1, blockPos, player.unwrap(), true);
    }

    private static final String protMapPath = FfaSkyUtil.ffaSkyDir + "/protectionMap.json";

    private static final ProtectionBlock[] protMapBlocks =  {
            new ProtectionBlock(Blocks.AIR, true, null),
            new ProtectionBlock(Blocks.VOID_AIR, true, null)
    };
    private static final ProtectionBlock notListedBlock =
            new ProtectionBlock(null, false, "You can only break blocks placed by players.");
    private static final String outsideMessage = "You can't build here.";

    public static ProtectionMap protectionMap = ProtectionMap.importMap(
            protMapPath, protMapBlocks, notListedBlock, outsideMessage);

    public static void createProtectionMap(ServerPlayer player) {
        protectionMap = new ProtectionMap(player,
                ffaCorner1, ffaCorner2, protMapPath, protMapBlocks, notListedBlock, outsideMessage);
    }

}
