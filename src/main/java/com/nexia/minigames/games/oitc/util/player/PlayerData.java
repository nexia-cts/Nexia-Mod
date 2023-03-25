package com.nexia.minigames.games.oitc.util.player;

import com.nexia.minigames.games.oitc.OitcGameMode;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public OitcGameMode gameMode;

    public int kills;

    public boolean isSpectating;

    public boolean isDeathTime;

    public int deathTime;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.gameMode = OitcGameMode.LOBBY;
        this.kills = 0;
        this.isSpectating = false;
        this.isDeathTime = false;
        this.deathTime = 5;
    }

}
