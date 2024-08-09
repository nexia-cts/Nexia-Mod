package com.nexia.ffa.base.player;

import com.nexia.base.player.SavedPlayerData;

public class FFASavedPlayerData extends SavedPlayerData {
    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;
    public String savedInventory;

    public FFASavedPlayerData() {
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;
        this.deaths = 0;
        this.savedInventory = "";

        set(Integer.class, "kills", kills);
        set(Integer.class, "killstreak", killstreak);
        set(Integer.class, "bestKillstreak", bestKillstreak);
        set(Integer.class, "deaths", deaths);
        set(String.class, "savedInventory", savedInventory);
    }
}
