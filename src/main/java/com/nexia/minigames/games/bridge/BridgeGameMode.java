package com.nexia.minigames.games.bridge;
public class BridgeGameMode {
    String id;

    public static final BridgeGameMode PLAYING = new BridgeGameMode("playing");
    public static final BridgeGameMode SPECTATOR = new BridgeGameMode("spectator");

    public static final BridgeGameMode LOBBY = new BridgeGameMode("lobby");

    public BridgeGameMode(String id) {
        this.id = id;
    }
}