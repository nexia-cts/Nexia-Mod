package com.nexia.minigames.games.bedwars.util.player;
public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;
    }

}
