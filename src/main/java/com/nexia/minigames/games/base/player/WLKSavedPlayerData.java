package com.nexia.minigames.games.base.player;

import com.nexia.base.player.SavedPlayerData;

public class WLKSavedPlayerData extends SavedPlayerData {

    public int wins;

    public int losses;

    public int kills;
    public WLKSavedPlayerData() {
        super();
        this.wins = 0;
        this.losses = 0;
        this.kills = 0;
        set(Integer.class, "wins", wins);
        set(Integer.class, "losses", losses);
        set(Integer.class, "kills", kills);
    }

}
