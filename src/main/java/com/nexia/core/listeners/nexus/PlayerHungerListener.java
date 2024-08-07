package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import com.nexia.minigames.games.skywars.util.player.SkywarsPlayerData;
import com.nexia.nexus.api.event.player.PlayerFoodLevelsChangeEvent;

public class PlayerHungerListener {
    public void registerListener(){
        PlayerFoodLevelsChangeEvent.BACKEND.register(playerFoodLevelsChangeEvent ->  {
            NexiaPlayer player = new NexiaPlayer(playerFoodLevelsChangeEvent.getPlayer());

            // SkyWars
            SkywarsPlayerData playerData = (SkywarsPlayerData) PlayerDataManager.getDataManager(NexiaCore.SKYWARS_DATA_MANAGER).get(player);
            if(SkywarsGame.isSkywarsPlayer(player) && playerData.gameMode.equals(SkywarsGameMode.PLAYING)) return;

            // Duels
            DuelGameMode duelGameMode = ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).gameMode;
            PlayerGameMode gameMode = ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode;
            if(gameMode.equals(PlayerGameMode.LOBBY) && (duelGameMode != null && !duelGameMode.hasSaturation)) return;

            playerFoodLevelsChangeEvent.setFoodLevel(20);
            playerFoodLevelsChangeEvent.setCancelled(true);
        });
    }
}
