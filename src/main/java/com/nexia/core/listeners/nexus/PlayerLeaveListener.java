package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.nexus.api.event.player.PlayerDisconnectEvent;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class PlayerLeaveListener {
    public void registerListener() {
        PlayerDisconnectEvent.BACKEND.register(playerDisconnectEvent -> {
            NexiaPlayer player = new NexiaPlayer(playerDisconnectEvent.getPlayer());
            player.leaveAllGames();

            //setLeaveMessage(player, playerDisconnectEvent);

            if (playerDisconnectEvent.getPlayer().hasTag("bot")) return;
            CompletableFuture.runAsync(() -> PlayerDataManager.dataManagerMap.forEach((resourceLocation, playerDataManager) -> playerDataManager.removePlayerData(player)));
        });
    }



    private static void setLeaveMessage(NexiaPlayer player, PlayerDisconnectEvent playerDisconnectEvent){
        playerDisconnectEvent.setLeaveMessage(
                Component.text("[").color(ChatFormat.lineColor)
                        .append(Component.text("-", ChatFormat.failColor))
                        .append(Component.text("] ").color(ChatFormat.lineColor))
                        .append(Component.text(player.getRawName(), ChatFormat.failColor))
        );
    }
}
