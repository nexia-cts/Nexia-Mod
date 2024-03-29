package com.nexia.core.listeners.metis;

import com.combatreforged.metis.api.event.player.PlayerInteractBlockEvent;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;

public class PlayerInteractBlockListener {
    public static void registerListener(){
        PlayerInteractBlockEvent.BACKEND.register(playerInteractBlockEvent -> {
            Player player = playerInteractBlockEvent.getPlayer();
            if(FfaClassicUtil.isFfaPlayer(player)) playerInteractBlockEvent.setCancelled(true);
        });
    }
}
