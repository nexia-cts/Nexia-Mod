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
        set(Integer.class, "kills", kills);
        set(Integer.class, "killstreak", killstreak);
        set(Integer.class, "bestKillstreak", bestKillstreak);
        set(Integer.class, "deaths", deaths);

        this.elo = 0;
        this.rating = 1;
        set(Double.class, "elo", elo);
        set(Double.class, "rating", rating);
    }
}


