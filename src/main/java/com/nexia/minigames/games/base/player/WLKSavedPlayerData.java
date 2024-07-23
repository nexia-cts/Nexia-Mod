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
        try {
            buildField(Integer.class, "wins", wins);
            buildField(Integer.class, "losses", losses);
            buildField(Integer.class, "kills", kills);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
