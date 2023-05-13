package com.nexia.core.listeners;

import com.nexia.core.games.util.LobbyUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class PlayerRespawnListener {
    public static void registerListener(ServerPlayer player) {

        if(player == null) { return; }

        ServerLevel respawn = player.getServer().getLevel(player.getRespawnDimension());

        if(respawn == LobbyUtil.lobbyWorld) {
            LobbyUtil.giveItems(player);
        }

        //if(minecraftPlayer != null && BwUtil.isInBedWars(minecraftPlayer)) BwPlayerEvents.respawned(minecraftPlayer);
    }
}
