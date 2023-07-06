package com.nexia.minigames.games.oitc.util.player;

import com.nexia.minigames.games.oitc.OitcGameMode;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public OitcGameMode gameMode;

    public int kills;

    public int deathTime;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.gameMode = OitcGameMode.LOBBY;
        this.kills = 0;
        this.deathTime = 5;
    }

}
