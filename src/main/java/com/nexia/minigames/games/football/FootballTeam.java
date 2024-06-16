package com.nexia.minigames.games.football;

import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FootballTeam {

    public ArrayList<ServerPlayer> players;

    public int goals;

    public EntityPos spawnPosition;

    public FootballTeam(@NotNull ArrayList<ServerPlayer> players, @NotNull EntityPos spawnPosition) {
        this.players = players;
        this.goals = 0;
        this.spawnPosition = spawnPosition;
    }

    public boolean refreshTeam() {
        for(ServerPlayer player : this.players) {
            if(player == null || !FootballGame.isFootballPlayer(player)) {
                this.players.remove(player);
                if(this.players.isEmpty()) return false;
            }
        }
        return true;
    }

    public boolean addPlayer(ServerPlayer player) {
        return this.players.add(player);
    }
}
