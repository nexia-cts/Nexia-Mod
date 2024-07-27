package com.nexia.ffa;

public class FfaGameMode {

    String name;
    String id;
    public static final FfaGameMode CLASSIC = new FfaGameMode("FFA Classic", "classic");

    public static final FfaGameMode KITS = new FfaGameMode("Kit FFA", "kits");

    public static final FfaGameMode POT = new FfaGameMode("Pot FFA", "pot");

    public static final FfaGameMode SKY = new FfaGameMode("Sky FFA", "sky");

    public static final FfaGameMode UHC = new FfaGameMode("UHC FFA", "uhc");

    public FfaGameMode(String name, String id) {
        this.name = name;
        this.id = id;
    }
}
