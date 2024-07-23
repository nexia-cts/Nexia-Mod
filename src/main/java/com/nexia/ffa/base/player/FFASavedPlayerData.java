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
            buildField(Integer.class, "kills", kills, FFASavedPlayerData.class);
            buildField(Integer.class, "killstreak", killstreak, FFASavedPlayerData.class);
            buildField(Integer.class, "bestKillstreak", bestKillstreak, FFASavedPlayerData.class);
            buildField(Integer.class, "deaths", deaths, FFASavedPlayerData.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
