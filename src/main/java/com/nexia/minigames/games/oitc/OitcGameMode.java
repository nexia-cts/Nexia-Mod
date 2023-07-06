package com.nexia.minigames.games.oitc;
public class OitcGameMode {
    String id;

    public static final OitcGameMode PLAYING = new OitcGameMode("playing");
    public static final OitcGameMode SPECTATOR = new OitcGameMode("spectator");

    public static final OitcGameMode LOBBY = new OitcGameMode("lobby");

    public OitcGameMode(String id) {
        this.id = id;
    }
}
