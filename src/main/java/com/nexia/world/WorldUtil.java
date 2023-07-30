package com.nexia.world;

import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.World;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WorldUtil {
    public static World getWorld(@NotNull Level level) {
        String[] name = level.dimension().toString().replaceAll("dimension / ", "").replaceAll("]", "").split(":");
        return ServerTime.factoryServer.getWorld(new Identifier(name[1], name[2]));
    }
}