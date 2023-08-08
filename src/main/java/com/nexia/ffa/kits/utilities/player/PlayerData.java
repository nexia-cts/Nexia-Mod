package com.nexia.ffa.kits.utilities.player;

import com.nexia.ffa.kits.FfaKit;

public class PlayerData {
    public SavedPlayerData savedData;
    public FfaKit kit;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;
        this.kit = null;
    }
}
