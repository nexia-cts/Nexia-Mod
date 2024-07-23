package com.nexia.minigames.games.football.util.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.minigames.games.football.FootballGameMode;
import com.nexia.minigames.games.football.FootballTeam;

public class FootballPlayerData extends PlayerData {

    // Stuff not saved in files
    public FootballGameMode gameMode;

    public FootballTeam team;

    public FootballPlayerData(SavedPlayerData savedData) {
        super(savedData);

        this.gameMode = FootballGameMode.LOBBY;
        this.team = null;
    }

}