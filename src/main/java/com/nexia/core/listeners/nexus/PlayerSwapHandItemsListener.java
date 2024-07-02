package com.nexia.core.listeners.nexus;

import com.nexia.nexus.api.event.player.PlayerSwapHandItemsEvent;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.NexiaPlayer;

public class PlayerSwapHandItemsListener {
    public void registerListener(){
        PlayerSwapHandItemsEvent.BACKEND.register(playerDropItemEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerDropItemEvent.getPlayer());

            if(LobbyUtil.isLobbyWorld(player.getWorld())) {
                playerDropItemEvent.setCancelled(true);
                return;
            }
        });
    }
}
