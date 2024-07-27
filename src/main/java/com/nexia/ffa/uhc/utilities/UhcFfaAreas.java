package com.nexia.ffa.uhc.utilities;

import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.ffa.FfaAreas;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.util.Location;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class UhcFfaAreas implements FfaAreas {

    public static boolean shouldResetMap = true;

    public static ServerLevel ffaWorld = null;
    public static World nexusFfaWorld = null;
    public static Location nexusFfaLocation = null;

    public static EntityPos spawn = new EntityPos(0.5, 128.0, -7.5, 0, 0);

    private static final int mapRadius = 80;

    public static final int buildLimitY = 120;

    public static EntityPos mapCenterSpawn = new EntityPos(0.5, 128.0, 0.5, 0, 0);
    public static BlockPos spawnCorner1 = mapCenterSpawn.toBlockPos().offset(-14, -12, -14);
    public static BlockPos spawnCorner2 = mapCenterSpawn.toBlockPos().offset(14, 12, 14);

    public static BlockPos ffaCorner1 = mapCenterSpawn.toBlockPos().offset(-mapRadius, -mapCenterSpawn.y, -mapRadius);
    public static BlockPos ffaCorner2 = mapCenterSpawn.toBlockPos().offset(mapRadius, mapCenterSpawn.y + 255, mapRadius);

    public static StructureMap map = new StructureMap(new Identifier("ffa", "uhc"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-80, -17, -80), true);

    public UhcFfaAreas() {
    }

    public static void resetMap(boolean announce) {

        map.pasteMap(ffaWorld);

        if (NexiaCore.config.debugMode) NexiaCore.logger.info("[DEBUG]: Uhc FFA Map has been reset.");

        if (announce) {
            for (Player player : nexusFfaWorld.getPlayers()) {
                player.sendMessage(Component.text("[!] Map has been reloaded!").color(ChatFormat.lineTitleColor));
            }
        }
    }

    public boolean isFfaWorld(Level level) {
        return isFfaWorldStatic(level);
    }

    public static boolean isFfaWorldStatic(Level level) {
        return level.dimension().location().toString().equals("ffa:uhc");
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
