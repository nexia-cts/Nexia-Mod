package com.nexia.minigames.games.duels;

import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DuelsSpawn {
    public static ServerLevel duelWorld = null;
    public static EntityPos spawn = new EntityPos(0, 80, 0, 0, 0);

    public DuelsSpawn() {
    }

    public static boolean isDuelsWorld(Level level) {
        return level.dimension().toString().contains("duels:hub");
    }

    public static boolean isInHub(Player player) {
        return player.level == duelWorld;
    }

    public static void setDuelWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isDuelsWorld(level)) {
                duelWorld = level;
                break;
            }
        }
    }
}
