package com.nexia.ffa.classic.utilities.player;

import com.nexia.base.player.SavedPlayerData;

public class ClassicFFASavedPlayerData extends SavedPlayerData {

    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;
    public double rating;
    public double elo;
    public ClassicFFASavedPlayerData() {
        super();
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;
        this.deaths = 0;
        try {
            buildField(Integer.class, "kills", kills, ClassicFFASavedPlayerData.class);
            buildField(Integer.class, "killstreak", killstreak, ClassicFFASavedPlayerData.class);
            buildField(Integer.class, "bestKillstreak", bestKillstreak, ClassicFFASavedPlayerData.class);
            buildField(Integer.class, "deaths", deaths, ClassicFFASavedPlayerData.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        this.elo = 0;
        this.rating = 1;
        try {
            buildField(Double.class, "elo", elo, ClassicFFASavedPlayerData.class);
            buildField(Double.class, "rating", rating, ClassicFFASavedPlayerData.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}


