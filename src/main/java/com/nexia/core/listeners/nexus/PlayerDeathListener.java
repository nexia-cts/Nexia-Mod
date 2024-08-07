package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.nexus.api.event.player.PlayerDeathEvent;

public class PlayerDeathListener {
    public void registerListener() {
        PlayerDeathEvent.BACKEND.register(playerDeathEvent -> {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(playerDeathEvent.getPlayer());

            PlayerGameMode gameMode = ((CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode;
            DuelsPlayerData duelsData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer);

            /*
            if(FfaUtil.isFfaPlayer(nexiaPlayer)) {
                FfaUtil.leaveOrDie(nexiaPlayer, playerDeathEvent, false);
            }
            */

            if (BwAreas.isBedWarsWorld(nexiaPlayer.getWorld())) {
                BwPlayerEvents.death(nexiaPlayer);
                return;
            }

            if(gameMode == PlayerGameMode.OITC){
                OitcGame.death(nexiaPlayer, playerDeathEvent);
                return;
            }

            if(gameMode == PlayerGameMode.SKYWARS) {
                SkywarsGame.death(nexiaPlayer, playerDeathEvent);
                return;
            }

            /*
            if(gameMode == PlayerGameMode.LOBBY && duelsData.gameOptions != null) {
                if(duelsData.gameOptions.duelsGame != null) {
                    duelsData.gameOptions.duelsGame.death(nexiaPlayer, playerDeathEvent);
                    return;
                }
                if(duelsData.gameOptions.teamDuelsGame != null) {
                    duelsData.gameOptions.teamDuelsGame.death(nexiaPlayer, playerDeathEvent);
                    return;
                }
            }
            */
        });
    }
}
