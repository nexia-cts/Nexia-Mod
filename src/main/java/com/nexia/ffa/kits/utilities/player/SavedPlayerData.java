package com.nexia.ffa.kits.utilities.player;

public class SavedPlayerData {

    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;
    public int rating;
    public int relative_increase;
    public int relative_decrease;
    public SavedPlayerData() {
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;

        this.deaths = 0;

        this.rating = 50;
        this.relative_increase = 0;
        this.relative_decrease = 0;
    }
}
