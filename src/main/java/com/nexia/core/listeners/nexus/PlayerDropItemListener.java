package com.nexia.core.listeners.nexus;

import com.nexia.nexus.api.event.player.PlayerHotbarDropItemEvent;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.base.player.NexiaPlayer;

public class PlayerDropItemListener {
    public void registerListener(){
        PlayerHotbarDropItemEvent.BACKEND.register(playerDropItemEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerDropItemEvent.getPlayer());

            if (!EventUtil.dropItem(player, playerDropItemEvent.getItemStack())) {
                playerDropItemEvent.setCancelled(true);
                player.refreshInventory();
                return;
            }
        });
    }
}
