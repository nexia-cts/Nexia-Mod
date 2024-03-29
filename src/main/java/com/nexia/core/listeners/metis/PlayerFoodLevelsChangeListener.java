package com.nexia.core.listeners.metis;

import com.combatreforged.metis.api.event.player.PlayerFoodLevelsChangeEvent;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;

public class PlayerFoodLevelsChangeListener {
    public static void registerListener(){
        PlayerFoodLevelsChangeEvent.BACKEND.register(playerFoodLevelsChangeEvent -> {

            Player player = playerFoodLevelsChangeEvent.getPlayer();

            // Duels
            DuelGameMode duelGameMode = PlayerDataManager.get(player).gameMode;
            PlayerGameMode gameMode = com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode;
            if(gameMode.equals(PlayerGameMode.LOBBY) && !duelGameMode.hasSaturation) return;

            playerFoodLevelsChangeEvent.setCancelled(true);
        });
    }
}
