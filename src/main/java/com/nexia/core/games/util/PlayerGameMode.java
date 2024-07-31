package com.nexia.core.games.util;

import com.nexia.minigames.games.bedwars.players.BwPlayers;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlayerGameMode {

    public static ArrayList<PlayerGameMode> playerGameModes = new ArrayList<>();

    public static ArrayList<String> stringPlayerGameModes = new ArrayList<>();

    public String id;

    public String name;

    public String tag;

    public static final PlayerGameMode LOBBY = new PlayerGameMode("lobby", "Hub", LobbyUtil.NO_DAMAGE_TAG);
    public static final PlayerGameMode BEDWARS = new PlayerGameMode("bedwars", "Bedwars", BwPlayers.BED_WARS_IN_GAME_TAG);

    public static final PlayerGameMode SKYWARS = new PlayerGameMode("skywars", "Skywars", SkywarsGame.SKYWARS_TAG);
    public static final PlayerGameMode OITC = new PlayerGameMode("oitc", "OITC", OitcGame.OITC_TAG);
    public static final PlayerGameMode FFA = new PlayerGameMode("ffa", "FFA", "ffa");
    public static final PlayerGameMode FOOTBALL = new PlayerGameMode("football", "Football", FootballGame.FOOTBALL_TAG);

    //public static final PlayerGameMode DUELS = new PlayerGameMode("duels");

    PlayerGameMode(String id, String name, String tag) {
        this.id = id;
        this.name = name;
        this.tag = tag;

        if(id.equals("lobby")) return;
        PlayerGameMode.playerGameModes.add(this);
        PlayerGameMode.stringPlayerGameModes.add(id);
    }

    public static PlayerGameMode identifyGamemode(@NotNull String gameMode) {

        for(PlayerGameMode playerGameMode : PlayerGameMode.playerGameModes) {
            if(playerGameMode.id.equalsIgnoreCase(gameMode)) return playerGameMode;
        }
        return null;


    }
}
