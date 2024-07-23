package com.nexia.minigames.games.duels.util.player;

import com.nexia.base.player.SavedPlayerData;

public class DuelsSavedPlayerData extends SavedPlayerData {

    public int wins;

    public int losses;

    public DuelsSavedPlayerData() {
        this.wins = 0;
        this.losses = 0;
        try {
            buildField(Integer.class, "wins", wins);
            buildField(Integer.class, "losses", losses);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
