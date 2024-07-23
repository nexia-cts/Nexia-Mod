package com.nexia.minigames.games.duels.util.player;

import com.nexia.base.player.SavedPlayerData;
import com.nexia.ffa.base.player.FFASavedPlayerData;

public class DuelsSavedPlayerData extends SavedPlayerData {

    public int wins;

    public int losses;

    public DuelsSavedPlayerData() {
        this.wins = 0;
        this.losses = 0;
        try {
            buildField(Integer.class, "wins", wins, FFASavedPlayerData.class);
            buildField(Integer.class, "losses", losses, FFASavedPlayerData.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
