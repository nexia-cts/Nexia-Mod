package com.nexia.minigames.games.football.util.player;

import com.nexia.minigames.games.football.FootballGameMode;
import com.nexia.minigames.games.football.FootballTeam;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public FootballGameMode gameMode;

    public FootballTeam team;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.gameMode = FootballGameMode.LOBBY;
        this.team = null;
    }

}