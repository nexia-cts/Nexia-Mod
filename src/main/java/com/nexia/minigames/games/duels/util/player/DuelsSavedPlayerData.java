package com.nexia.minigames.games.duels.util.player;

import com.nexia.base.player.SavedPlayerData;

public class DuelsSavedPlayerData extends SavedPlayerData {

    public int wins;

    public int losses;

    public DuelsSavedPlayerData() {
        this.wins = 0;
        this.losses = 0;
        set(Integer.class, "wins", wins);
        set(Integer.class, "losses", losses);
    }

}
