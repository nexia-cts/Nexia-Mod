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
        try {
            buildField(Integer.class, "wins", wins, FootballSavedPlayerData.class);
            buildField(Integer.class, "losses", losses, FootballSavedPlayerData.class);
            buildField(Integer.class, "goals", goals, FootballSavedPlayerData.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
