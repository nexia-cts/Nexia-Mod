package com.nexia.minigames.games.oitc;

import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class OitcSpawn {
    public static ServerLevel oitcWorld = null;
    public static EntityPos spawn = new EntityPos(0, 80, 0, 0, 0);

    public OitcSpawn() {
    }

    public static boolean isOitcWorld(Level level) {
        return level.dimension().toString().contains("oitc:hub");
    }

    public static boolean isInHub(Player player) {
        return player.level == oitcWorld;
    }

    public static void setOitcWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isOitcWorld(level)) {
                oitcWorld = level;
                break;
            }
        }
    }
}
