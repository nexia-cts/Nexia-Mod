package com.nexia.minigames.games.football.util.player;

import com.nexia.base.player.SavedPlayerData;

public class FootballSavedPlayerData extends SavedPlayerData {

    public int wins;

    public int losses;

    public int goals;

    public FootballSavedPlayerData() {
        super();
        this.wins = 0;
        this.losses = 0;
        this.goals = 0;
        set(Integer.class, "wins", wins);
        set(Integer.class, "losses", losses);
        set(Integer.class, "goals", goals);
    }

}
