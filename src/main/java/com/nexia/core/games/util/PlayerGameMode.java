package com.nexia.core.games.util;
public class PlayerGameMode {

    String id;


    public static final PlayerGameMode LOBBY = new PlayerGameMode("lobby");
    public static final PlayerGameMode SKYWARS = new PlayerGameMode("skywars");

    public static final PlayerGameMode BEDWARS = new PlayerGameMode("bedwars");
    public static final PlayerGameMode FFA = new PlayerGameMode("ffa");

    PlayerGameMode(String id) {
        this.id = id;
    }

}
