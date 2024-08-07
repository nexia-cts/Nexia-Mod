package com.nexia.core.listeners.nexus;

import com.nexia.nexus.api.event.player.PlayerSwapHandItemsEvent;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.base.player.NexiaPlayer;

public class PlayerSwapHandItemsListener {
    public void registerListener(){
        PlayerSwapHandItemsEvent.BACKEND.register(playerSwapHandItemsEvent -> {
            NexiaPlayer player = new NexiaPlayer(playerSwapHandItemsEvent.getPlayer());

            if(LobbyUtil.isLobbyWorld(player.getWorld())) {
                playerSwapHandItemsEvent.setCancelled(true);
            }
        });
    }
}
