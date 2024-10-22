package com.nexia.minigames.games.bridge;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BridgeTeam {

    public ArrayList<NexiaPlayer> players;

    public int goals;

    public EntityPos spawnPosition;

    public BridgeTeam(@NotNull ArrayList<NexiaPlayer> players, @NotNull EntityPos spawnPosition) {
        this.players = players;
        this.goals = 0;
        this.spawnPosition = spawnPosition;
    }

    public boolean refreshTeam() {
        for(NexiaPlayer player : this.players) {
            if(player.unwrap() == null || !BridgeGame.isBridgePlayer(player)) {
                this.removePlayer(player);
                if(this.players.isEmpty()) return false;
            }
        }
        return true;
    }

    public boolean addPlayer(NexiaPlayer player) {
        return this.players.add(player);
    }

    public boolean removePlayer(NexiaPlayer player) {
        return this.players.remove(player);
    }
}
