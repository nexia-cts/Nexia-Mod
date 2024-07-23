package com.nexia.minigames.games.oitc.util.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.minigames.games.oitc.OitcGameMode;

public class OITCPlayerData extends PlayerData {

    // Stuff not saved in files
    public OitcGameMode gameMode;

    public int kills;

    public boolean hasDied;

    public OITCPlayerData(SavedPlayerData savedData) {
        super(savedData);

        this.gameMode = OitcGameMode.LOBBY;
        this.kills = 0;
        this.hasDied = false;
    }

}