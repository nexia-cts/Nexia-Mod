package com.nexia.minigames.games.bedwars.util.player;

import com.nexia.base.player.SavedPlayerData;

public class BedwarsSavedPlayerData extends SavedPlayerData {

    public int wins;
    public int losses;
    public int bedsBroken;
    public BedwarsSavedPlayerData() {
        super(new Data());
        this.wins = 0;
        this.losses = 0;
        this.bedsBroken = 0;
        set(Integer.class, "wins", wins);
        set(Integer.class, "losses", losses);
        set(Integer.class, "bedsBroken", bedsBroken);
    }

}
