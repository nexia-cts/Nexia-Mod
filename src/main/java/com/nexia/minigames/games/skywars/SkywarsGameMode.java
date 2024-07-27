package com.nexia.minigames.games.skywars;

public class SkywarsGameMode {

    public static final SkywarsGameMode PLAYING = new SkywarsGameMode("playing");
    public static final SkywarsGameMode SPECTATOR = new SkywarsGameMode("spectator");

    public static final SkywarsGameMode LOBBY = new SkywarsGameMode("lobby");

    String id;

    public SkywarsGameMode(String id) {
        this.id = id;
    }
}
