package com.nexia.core.utilities.world;

import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.api.world.World;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldUtil {

    public static final RuntimeWorldConfig defaultWorldConfig = new RuntimeWorldConfig()
            .setDimensionType(DimensionType.OVERWORLD_LOCATION)
            .setGenerator(getChunkGenerator(Biomes.THE_VOID))
            .setDifficulty(Difficulty.HARD)
            .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
            .setGameRule(GameRules.RULE_MOBGRIEFING, false)
            .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
            .setGameRule(GameRules.RULE_DAYLIGHT, false)
            .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
            .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
            .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
            .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0);

    private static final String templateWorldName = "template:void";

    public static boolean isTemplateVoidWorld(Level level) {
        return level.dimension().location().toString().equals(templateWorldName);
    }

    public static ServerLevel templateWorld;

    public static void setVoidWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isTemplateVoidWorld(level)) {
                templateWorld = level;
                break;
            }
        }
    }

    public static World getWorld(@NotNull Level level) {
        return ServerTime.nexusServer.getWorld(new Identifier(level.dimension().location().getNamespace(), level.dimension().location().getPath()));
    }

    public static void deleteWorld(ResourceLocation identifier) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(identifier.getNamespace(), identifier.getPath())).location(),
                    new RuntimeWorldConfig());
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + identifier.getNamespace(), identifier.getPath()));
            ServerTime.nexusServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getPath(), false);
        } catch (Exception e) {
            NexiaCore.logger.error("Error occurred while deleting world: {}:{}", identifier.getNamespace(), identifier.getPath());

            try {
                ServerTime.nexusServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getPath(), false);
            } catch (Exception ignored2) {
                if(NexiaCore.config.debugMode) e.printStackTrace();
            }

            if(NexiaCore.config.debugMode) e.printStackTrace();
            return;
        }
        worldHandle.delete();
    }

    public static ChunkGenerator getChunkGenerator(@NotNull ResourceKey<Biome> biome) {
        try {
            return new VoidChunkGenerator(BuiltinRegistries.BIOME, biome);
        } catch (Exception exception) {
            if (templateWorld == null || templateWorld.getChunkSource().getGenerator() == null) return ServerTime.minecraftServer.overworld().getChunkSource().getGenerator();
            return templateWorld.getChunkSource().getGenerator();
        }
    }

    public static void deleteTempWorlds() {
        if(NexiaCore.config.debugMode) NexiaCore.logger.info("[DEBUG]: Deleting Temporary Worlds");
        List<ResourceLocation> delete = new ArrayList<>();

        for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            ResourceLocation name = level.dimension().location();
            if (name.getNamespace().equalsIgnoreCase("duels") ||
                    (name.getNamespace().equalsIgnoreCase("skywars") && !name.getPath().equalsIgnoreCase(SkywarsGame.id)) ||
                    name.getNamespace().equalsIgnoreCase("kitroom")
            ) {
                delete.add(name);
            }
        }

        for (ResourceLocation deletion : delete) {
            WorldUtil.deleteWorld(deletion);
        }
    }
}