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
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;

public class WorldUtil {
    public static World getWorld(@NotNull Level level) {
        String[] name = level.dimension().toString().replaceAll("dimension / ", "").replaceAll("]", "").split(":");
        return ServerTime.factoryServer.getWorld(new Identifier(name[1], name[2]));
    }

    public static void deleteWorld(Identifier identifier) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(identifier.getNamespace(), identifier.getId())).location(),
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