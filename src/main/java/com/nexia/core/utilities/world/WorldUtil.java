package com.nexia.core.utilities.world;

import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.api.world.World;
import com.nexia.core.Main;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldUtil {

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
            Main.logger.error("Error occurred while deleting world: {}:{}", identifier.getNamespace(), identifier.getPath());

            try {
                ServerTime.nexusServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getPath(), false);
            } catch (Exception ignored2) {
                if(Main.config.debugMode) e.printStackTrace();
            }

            if(Main.config.debugMode) e.printStackTrace();
            return;
        }
        worldHandle.delete();
    }

    public static ChunkGenerator getChunkGenerator() {
        // return new VoidChunkGenerator(BuiltinRegistries.BIOME, Biomes.PLAINS)
        // doesnt work ^^ extremely buggy

        if(templateWorld == null || templateWorld.getChunkSource().getGenerator() == null) return ServerTime.minecraftServer.overworld().getChunkSource().getGenerator();
        return templateWorld.getChunkSource().getGenerator();
    }

    public static void deleteTempWorlds() {
        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Deleting Temporary Worlds");
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