package com.nexia.ffa;

public class FfaGameMode {

    String name;
    String id;
    public static final FfaGameMode CLASSIC = new FfaGameMode("FFA Classic", "classic");

    public static final FfaGameMode KITS = new FfaGameMode("Kit FFA", "kits");

    public FfaGameMode(String name, String id) {
        this.name = name;
        this.id = id;
    }
}
