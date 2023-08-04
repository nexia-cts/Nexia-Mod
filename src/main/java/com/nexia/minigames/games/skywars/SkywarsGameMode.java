package com.nexia.minigames.games.skywars;
public class SkywarsGameMode {
    String id;

    public static final SkywarsGameMode PLAYING = new SkywarsGameMode("playing");
    public static final SkywarsGameMode SPECTATOR = new SkywarsGameMode("spectator");

    public static final SkywarsGameMode LOBBY = new SkywarsGameMode("lobby");

    public SkywarsGameMode(String id) {
        this.id = id;
    }
}