package com.nexia.ffa.uhc.utilities;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.pos.ProtectionBlock;
import com.nexia.core.utilities.pos.ProtectionMap;
import com.nexia.ffa.Main;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import static com.nexia.minigames.games.bedwars.areas.BwAreas.protectionMap;

public class FfaAreas {
    public static ServerLevel ffaWorld = null;

    public static EntityPos spawn = new EntityPos(Main.uhc.spawnCoordinates[0], Main.uhc.spawnCoordinates[1], Main.uhc.spawnCoordinates[2], 0, 0);

    private static final int mapRadius = 80;

    public static final int buildLimitY = 120;

    public static EntityPos mapCenterSpawn = new EntityPos(Main.uhc.mapCenterCoordinates[0], Main.uhc.mapCenterCoordinates[1], Main.uhc.mapCenterCoordinates[2], 0, 0);
    public static BlockPos spawnCorner1 = mapCenterSpawn.toBlockPos().offset(-14, -12, -14);
    public static BlockPos spawnCorner2 = mapCenterSpawn.toBlockPos().offset(14, 12, 14);

    public static BlockPos ffaCorner1 = mapCenterSpawn.toBlockPos().offset(-mapRadius, -mapCenterSpawn.y, -mapRadius);
    public static BlockPos ffaCorner2 = mapCenterSpawn.toBlockPos().offset(mapRadius, mapCenterSpawn.y + 255, mapRadius);

    public FfaAreas() {
    }

    public static boolean isFfaWorld(Level level) {
        return level.dimension().toString().contains(Main.uhc.worldName);
    }

    public static boolean isInFfaSpawn(Player player) {
        BlockVec3 pos = new BlockVec3(player.position());

        return pos.x >= spawnCorner1.getX() && pos.x <= spawnCorner2.getX() &&
                pos.y >= spawnCorner1.getY() && pos.y <= spawnCorner2.getY() &&
                pos.z >= spawnCorner1.getZ() && pos.z <= spawnCorner2.getZ();
    }

    public static boolean canBuild(ServerPlayer player, BlockPos blockPos) {
        if (protectionMap == null) {
            PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("Something went wrong, please inform the admins").color(ChatFormat.failColor));
            return false;
        }

        return protectionMap.canBuiltAt(ffaCorner1, blockPos, player, true);
    }

    private static final String protMapPath = FfaUhcUtil.ffaUhcDir + "/protectionMap.json";

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

    public static void setFfaWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isFfaWorld(level)) {
                ffaWorld = level;
                break;
            }
        }
    }
}
