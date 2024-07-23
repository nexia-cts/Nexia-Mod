package com.nexia.ffa.kits.utilities.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.ffa.kits.FfaKit;

public class KitFFAPlayerData extends PlayerData {
    public FfaKit kit;

    public KitFFAPlayerData(SavedPlayerData savedData) {
        super(savedData);
        this.kit = null;
    }
}
