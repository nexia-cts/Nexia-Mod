package com.nexia.minigames.games.bridge.util.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.minigames.games.bridge.BridgeGameMode;
import com.nexia.minigames.games.bridge.BridgeTeam;

public class BridgePlayerData extends PlayerData {

    // Stuff not saved in files
    public BridgeGameMode gameMode;

    public BridgeTeam team;

    public BridgePlayerData(SavedPlayerData savedData) {
        super(savedData);

        this.gameMode = BridgeGameMode.LOBBY;
        this.team = null;
    }

}