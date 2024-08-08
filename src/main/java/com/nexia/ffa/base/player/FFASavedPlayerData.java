package com.nexia.ffa.base.player;

import com.nexia.base.player.SavedPlayerData;

public class FFASavedPlayerData extends SavedPlayerData {

    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;

    public FFASavedPlayerData() {
        super();
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;

        this.deaths = 0;
        set(Integer.class, "kills", kills);
        set(Integer.class, "killstreak", killstreak);
        set(Integer.class, "bestKillstreak", bestKillstreak);
        set(Integer.class, "deaths", deaths);
    }
}
