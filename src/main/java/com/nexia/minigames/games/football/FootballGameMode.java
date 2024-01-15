package com.nexia.minigames.games.football;
public class FootballGameMode {
    String id;

    public static final FootballGameMode PLAYING = new FootballGameMode("playing");
    public static final FootballGameMode SPECTATOR = new FootballGameMode("spectator");

    public static final FootballGameMode LOBBY = new FootballGameMode("lobby");

    public FootballGameMode(String id) {
        this.id = id;
    }
}