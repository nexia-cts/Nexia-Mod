package com.nexia.ffa.kits.utilities.player;

public class SavedPlayerData {

    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;
    public double rating;
    public double uniqueOpponents;
    public double totalFights;
    public SavedPlayerData() {
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;

        this.deaths = 0;

        this.rating = 1;
        this.uniqueOpponents = 0;
        this.totalFights = 0;
    }
}
