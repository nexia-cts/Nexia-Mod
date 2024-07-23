package com.nexia.minigames.games.skywars.util.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.minigames.games.skywars.SkywarsGameMode;

public class SkywarsPlayerData extends PlayerData {

    // Stuff not saved in files
    public SkywarsGameMode gameMode;

    public int kills;
    public SkywarsPlayerData(SavedPlayerData savedData) {
        super(savedData);

        this.gameMode = SkywarsGameMode.LOBBY;
        this.kills = 0;
    }
}
