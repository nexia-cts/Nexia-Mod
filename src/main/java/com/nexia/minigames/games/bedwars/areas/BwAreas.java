package com.nexia.minigames.games.bedwars.areas;

import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.pos.ProtectionBlock;
import com.nexia.core.utilities.pos.ProtectionMap;
import com.nexia.minigames.games.bedwars.BwGame;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BwAreas {

    public static ServerLevel bedWarsWorld = null;
    public static EntityPos bedWarsCenter = new EntityPos(0.5, 50, 0.5, 0, 0);

    private static final int bedWarsRadius = 120;
    public static BlockPos bedWarsCorner1 = bedWarsCenter.toBlockPos().offset(-bedWarsRadius, -bedWarsCenter.y, -bedWarsRadius);
    public static BlockPos bedWarsCorner2 = bedWarsCenter.toBlockPos().offset(bedWarsRadius, -bedWarsCenter.y + 255, bedWarsRadius);
    public static int buildLimit = 80;

    public static EntityPos spectatorSpawn = bedWarsCenter.c().add(0, 20, 0);

    public static EntityPos queueSpawn = bedWarsCenter.c().add(0, -bedWarsCenter.y + 100, 0);
    public static BlockVec3 queueC1 = queueSpawn.toBlockVec3().add(-8, -1, -8);
    public static BlockVec3 queueC2 = queueSpawn.toBlockVec3().add(8, 7, 8);

    // Protection map related

    private static final String protMapFileName = "protectionMap.json";
    private static final String protMapFilePath = BwGame.bedWarsDirectory + "/" + protMapFileName;

    public static ProtectionBlock[] protMapBlocks = {
            new ProtectionBlock(Blocks.VOID_AIR, true, null),
            new ProtectionBlock(Blocks.AIR, true, null),
            new ProtectionBlock(Blocks.BROWN_STAINED_GLASS, false, "You can't build here."),
            new ProtectionBlock(Blocks.BROWN_STAINED_GLASS_PANE, false, "You can't build this close to generators."),
    };
    public static ProtectionBlock notListedMapBlock =
            new ProtectionBlock(null, false, "You can only break blocks placed by players.");
    public static String outsideMessage = "You have reached the build limit.";

    public static ProtectionMap protectionMap = ProtectionMap.importMap(
            protMapFilePath, protMapBlocks, notListedMapBlock, outsideMessage);

    public static void createProtectionMap(ServerPlayer player) {
        protectionMap = new ProtectionMap(player,
                bedWarsCorner1, bedWarsCorner2, protMapFilePath, protMapBlocks, notListedMapBlock, outsideMessage);
    }

    // ------------------------------------------------------------

    public static boolean isBedWarsWorld(Level level) {
        return level.dimension().equals(BwDimension.LEVEL_KEY) || level.dimension().toString().contains(BwDimension.DIMENSION_ID + ":" + BwDimension.DIMENSION_NAME);
    }

    public static void setBedWarsWorld(MinecraftServer minecraftServer) {
        for (ServerLevel level : minecraftServer.getAllLevels()) {
            if (isBedWarsWorld(level)) {
                bedWarsWorld = level;
                return;
            }
        }
    }

    private static boolean isInsideBorder(BlockPos mapPos, byte[][][] map) {
        return mapPos.getX() >= 0 && mapPos.getX() < map.length &&
                mapPos.getY() >= 0 && mapPos.getY() < map[0].length &&
                mapPos.getZ() >= 0 && mapPos.getZ() < map[0][0].length;
    }

    public static boolean isImmuneBlock(BlockPos blockPos) {
        return !canBuildAt(null, blockPos, false);
    }

    public static boolean canBuildAt(@Nullable ServerPlayer player, BlockPos blockPos, boolean sendMessage) {
        ProtectionMap protectionMap = BwAreas.protectionMap;
        BlockPos mapPos = blockPos.subtract(bedWarsCorner1);
        sendMessage = sendMessage && player != null;

        Player nexusPlayer = null;

        if(player != null) {
            nexusPlayer = PlayerUtil.getNexusPlayer(player);
        }

        if (protectionMap == null) {
            if (sendMessage) {
                nexusPlayer.sendMessage(Component.text("An error occurred, please inform the admins.").color(ChatFormat.failColor));
            }
            return false;
        }

        if ((player != null && !isBedWarsWorld(player.getLevel())) || !isInsideBorder(mapPos, protectionMap.map)) {
            if (sendMessage) {
                nexusPlayer.sendMessage(Component.text("You have reached the built limit.").color(ChatFormat.failColor));
            }
            return false;
        }

        return protectionMap.canBuiltAt(bedWarsCorner1, blockPos, player, sendMessage);
    }

    public static void tick() {
        if (resetMap) {
            if (mapResetTicks >= bedWarsCorner1.getY() && mapResetTicks <= buildLimit) {
                clearOneLayer(mapResetTicks);
            }
            if (mapResetTicks > bedWarsCorner2.getY() && resetMap) {
                resetMap = false;
            }
            mapResetTicks++;
        }

    }

    // Map related  ---------------------------------------------------------------------------------

    private static boolean resetMap = false;
    private static int mapResetTicks = 0;

    public static void clearBedWarsMap() {
        resetMap = true;
        mapResetTicks = bedWarsCorner1.getY();
    }

    private static void clearOneLayer(int yLevel) {
        for (BlockPos blockPos : BlockPos.betweenClosed(
                bedWarsCorner1.getX(), yLevel, bedWarsCorner1.getZ(),
                bedWarsCorner2.getX(), yLevel, bedWarsCorner2.getZ())) {

            BlockState blockState = bedWarsWorld.getBlockState(blockPos);
            if (!(isImmuneBlock(blockPos) || blockState.getBlock() instanceof BedBlock)) {
                bedWarsWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public static void spawnQueueBuild() {

        for (BlockPos pos : BlockPos.betweenClosed(
                queueC1.x, queueC1.y, queueC1.z,
                queueC2.x, queueC2.y, queueC2.z)) {

            if (bedWarsWorld.getBlockState(pos).getBlock() == Blocks.AIR && (
                    queueC1.x == pos.getX() || queueC2.x == pos.getX() ||
                    queueC1.y == pos.getY() || queueC2.y == pos.getY() ||
                    queueC1.z == pos.getZ() || queueC2.z == pos.getZ() )) {

                bedWarsWorld.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
            }
        }
    }

    public static void clearQueueBuild() {
        for (BlockPos pos : BlockPos.betweenClosed(
                queueC1.x, queueC1.y, queueC1.z,
                queueC2.x, queueC2.y, queueC2.z)) {

            if (bedWarsWorld.getBlockState(pos).getBlock() == Blocks.GLASS) {
                bedWarsWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

}
