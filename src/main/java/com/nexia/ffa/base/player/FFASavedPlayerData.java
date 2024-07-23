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
        try {
            buildField(Integer.class, "kills", kills);
            buildField(Integer.class, "killstreak", killstreak);
            buildField(Integer.class, "bestKillstreak", bestKillstreak);
            buildField(Integer.class, "deaths", deaths);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
