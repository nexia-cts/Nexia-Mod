package com.nexia.minigames.games.duels.util;

import net.minecraft.server.level.ServerPlayer;

public class SavedDuelsData {
    private ServerPlayer winner, loser;

    public SavedDuelsData(ServerPlayer winner, ServerPlayer loser) {
        this.winner = winner;
        this.loser = loser;
    }

    public ServerPlayer getWinner() {
        return winner;
    }

    public ServerPlayer getLoser() {
        return loser;
    }
}