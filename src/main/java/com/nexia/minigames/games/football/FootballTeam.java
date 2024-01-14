package com.nexia.minigames.games.football;

import com.nexia.core.utilities.pos.EntityPos;
import net.notcoded.codelib.players.AccuratePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FootballTeam {

    public ArrayList<AccuratePlayer> players;

    public int goals;

    public EntityPos spawnPosition;

    public FootballTeam(@NotNull ArrayList<AccuratePlayer> players, @NotNull EntityPos spawnPosition) {
        this.players = players;
        this.goals = 0;
        this.spawnPosition = spawnPosition;
    }

    public boolean refreshTeam() {
        for(AccuratePlayer player : this.players) {
            if(player.get() == null || !FootballGame.isFootballPlayer(player.get())) {
                this.players.remove(player);
                if(this.players.isEmpty()) return false;
            }
        }
        return true;
    }

    public boolean addPlayer(AccuratePlayer player) {
        return this.players.add(player);
    }
}
