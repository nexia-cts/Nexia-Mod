package com.nexia.core.games.util;
public class PlayerGameMode {

    String id;

    public static final PlayerGameMode LOBBY = new PlayerGameMode("lobby");
    public static final PlayerGameMode FFA = new PlayerGameMode("ffa");

    //public static final PlayerGameMode DUELS = new PlayerGameMode("duels");

    PlayerGameMode(String id) {
        this.id = id;
    }

}
