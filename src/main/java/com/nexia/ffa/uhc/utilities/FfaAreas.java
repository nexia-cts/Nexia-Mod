package com.nexia.ffa.uhc.utilities;

import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.entity.Entity;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.pos.PositionUtil;
import com.nexia.core.utilities.world.StructureMap;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.ffa.Main;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.notcoded.codelib.util.world.structure.Rotation;

public class FfaAreas {

    public static boolean shouldResetMap = true;

    public static ServerLevel ffaWorld = null;

    public static EntityPos spawn = new EntityPos(Main.uhc.spawnCoordinates[0], Main.uhc.spawnCoordinates[1], Main.uhc.spawnCoordinates[2], 0, 0);

    private static final int mapRadius = 80;

    public static final int buildLimitY = 120;

    public static EntityPos mapCenterSpawn = new EntityPos(Main.uhc.mapCenterCoordinates[0], Main.uhc.mapCenterCoordinates[1], Main.uhc.mapCenterCoordinates[2], 0, 0);
    public static BlockPos spawnCorner1 = mapCenterSpawn.toBlockPos().offset(-14, -12, -14);
    public static BlockPos spawnCorner2 = mapCenterSpawn.toBlockPos().offset(14, 12, 14);

    public static BlockPos ffaCorner1 = mapCenterSpawn.toBlockPos().offset(-mapRadius, -mapCenterSpawn.y, -mapRadius);
    public static BlockPos ffaCorner2 = mapCenterSpawn.toBlockPos().offset(mapRadius, mapCenterSpawn.y + 255, mapRadius);


    public static StructureMap map = new StructureMap(new Identifier("ffa", "uhc"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-80, -17, -80), true);

    public FfaAreas() {
    }

    public static void resetMap(boolean announce) {

        map.pasteMap(ffaWorld);

        if(com.nexia.core.Main.config.debugMode) com.nexia.core.Main.logger.info("[DEBUG]: Uhc FFA Map has been reset.");

        if(announce){
            for(Entity entity : WorldUtil.getWorld(ffaWorld).getEntities()) {
                if(entity instanceof com.combatreforged.factory.api.world.entity.player.Player player && player.hasTag("ffa_uhc")) player.sendMessage(Component.text("[!] Map has been reloaded!").color(ChatFormat.lineTitleColor));
            }
        }
    }

    public static boolean isFfaWorld(Level level) {
        return level.dimension().toString().contains(Main.uhc.worldName);
    }

    public static boolean isInFfaSpawn(NexiaPlayer player) {
        return PositionUtil.isBetween(spawnCorner1, spawnCorner2, player.player().get().blockPosition());
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
