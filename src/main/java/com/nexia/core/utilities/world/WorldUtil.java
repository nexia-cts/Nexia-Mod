package com.nexia.core.utilities.world;

import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.api.world.World;
import com.nexia.core.Main;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldUtil {
    private static final String templateWorldName = "template:void";

    public static boolean isTemplateVoidWorld(Level level) {
        return getWorldName(level).equalsIgnoreCase(templateWorldName);
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
        return ServerTime.nexusServer.getWorld(WorldUtil.getIdentifierWorldName(level));
    }

    public static String getWorldName(@NotNull Level level) {
        return level.dimension().toString().replaceAll("dimension / ", "").replaceAll("]", "").replaceAll("ResourceKey\\[minecraft:", "");
    }

    public static Identifier getIdentifierWorldName(@NotNull Level level) {
        return getWorldName(getWorldName(level)); // why does this exist
    }

    public static Identifier getWorldName(String name) {
        String[] splitName = name.split(":");
        return new Identifier(splitName[0], splitName[1]);
    }

    public static void deleteWorld(Identifier identifier) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(identifier.getNamespace(), identifier.getId())).location(),
                    new RuntimeWorldConfig());
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + identifier.getNamespace(), identifier.getId()));
            ServerTime.nexusServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getId(), false);
        } catch (Exception e) {
            Main.logger.error("Error occurred while deleting world: {}:{}", identifier.getNamespace(), identifier.getId());

            try {
                ServerTime.nexusServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getId(), false);
            } catch (Exception ignored2) {
                if(Main.config.debugMode) e.printStackTrace();
            }

            if(Main.config.debugMode) e.printStackTrace();
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
        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Deleting Temporary Worlds");
        List<Identifier> delete = new ArrayList<>();

        for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            Identifier split = WorldUtil.getIdentifierWorldName(level);
            if (split.getNamespace().equalsIgnoreCase("duels") ||
                    (split.getNamespace().equalsIgnoreCase("skywars") && !split.getId().equalsIgnoreCase(SkywarsGame.id)) ||
                    split.getNamespace().equalsIgnoreCase("kitroom")
            ) {
                delete.add(split);
            }
        }

        for (Identifier deletion : delete) {
            WorldUtil.deleteWorld(deletion);
        }
    }
}