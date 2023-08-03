package com.nexia.world;

import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.World;
import com.nexia.core.Main;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;

public class WorldUtil {
    public static World getWorld(@NotNull Level level) {
        return ServerTime.factoryServer.getWorld(WorldUtil.getWorldName(WorldUtil.getWorldName(level)));
    }

    public static String getWorldName(@NotNull Level level) {
        return level.dimension().toString().replaceAll("dimension / ", "").replaceAll("]", "");
    }

    public static Identifier getWorldName(String name) {
        String[] splitName = name.split(":");
        return new Identifier(splitName[1], splitName[2]);
    }

    public static void deleteWorld(Identifier identifier) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    new ResourceLocation(identifier.getNamespace(), identifier.getId()),
                    new RuntimeWorldConfig());
            ServerTime.factoryServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getId(), false);
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + identifier.getNamespace(), identifier.getId()));
        } catch (Exception ignored) {
            Main.logger.error("Error occurred while deleting world: " + identifier.getNamespace() + ":" + identifier.getId());
            return;
        }
        worldHandle.delete();
    }
}