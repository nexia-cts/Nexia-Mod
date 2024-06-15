package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerSwapHandItemsEvent;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.NexiaPlayer;

public class PlayerSwapHandItemsListener {
    public void registerListener(){
        PlayerSwapHandItemsEvent.BACKEND.register(playerDropItemEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerDropItemEvent.getPlayer());

            if(LobbyUtil.isLobbyWorld(player.unwrap().getLevel())) {
                playerDropItemEvent.setCancelled(true);
                return;
            }
        });
    }
}
