package com.nexia.minigames.games.skywars.util.player;

import com.nexia.minigames.games.skywars.SkywarsGameMode;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public SkywarsGameMode gameMode;

    public int kills;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.gameMode = SkywarsGameMode.LOBBY;
        this.kills = 0;
    }

}