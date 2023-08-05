package com.nexia.core.games.util;
public class PlayerGameMode {

    String id;

    public int players;

    public static final PlayerGameMode LOBBY = new PlayerGameMode("lobby", 0);
    public static final PlayerGameMode SKYWARS = new PlayerGameMode("skywars", 0);

    public static final PlayerGameMode BEDWARS = new PlayerGameMode("bedwars", 0);
    public static final PlayerGameMode FFA = new PlayerGameMode("ffa", 0);

    PlayerGameMode(String id, int players) {
        this.id = id;
        this.players = players;
    }

}
