package com.nexia.minigames.games.duels;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class DuelGameMode {
    String id;

    public static String[] duels = {"LEAVE", "AXE", "SWORD_ONLY", "TRIDENT_ONLY", "HOE_ONLY", "FFA", "BOW_ONLY", "UHC", "VANILLA"};

    public static ArrayList<ServerPlayer> AXE_QUEUE = new ArrayList<>();
    public static final DuelGameMode AXE = new DuelGameMode("axe");

    public static ArrayList<ServerPlayer> SWORD_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only");

    public static ArrayList<ServerPlayer> TRIDENT_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only");

    public static ArrayList<ServerPlayer> HOE_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode HOE_ONLY = new DuelGameMode("hoe_only");

    public static ArrayList<ServerPlayer> BOW_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode BOW_ONLY = new DuelGameMode("bow");

    public static ArrayList<ServerPlayer> VANILLA_QUEUE = new ArrayList<>();
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla");

    public static ArrayList<ServerPlayer> UHC_QUEUE = new ArrayList<>();
    public static final DuelGameMode UHC = new DuelGameMode("uhc");

    public static ArrayList<ServerPlayer> FFA_QUEUE = new ArrayList<>();
    public static final DuelGameMode FFA = new DuelGameMode("ffa");

    public static final DuelGameMode LOBBY = new DuelGameMode("lobby");
    public DuelGameMode(String id) {
        this.id = id;
    }
}
