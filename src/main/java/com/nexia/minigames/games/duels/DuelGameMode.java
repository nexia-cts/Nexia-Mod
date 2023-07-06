package com.nexia.minigames.games.duels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;

public class DuelGameMode {
    public String id;
    public boolean hasRegen;

    public GameType gameMode;

    public static ArrayList<ItemStack> duelsMaps = new ArrayList<>();
    public static ArrayList<ItemStack> duelsItems = new ArrayList<>();
    public static String[] duels = {"AXE", "SWORD_ONLY", "SHIELD", "POT", "NETH_POT", "OG_VANILLA", "SMP", "TRIDENT_ONLY", "HOE_ONLY", "FFA", "BOW_ONLY", "UHC", "VANILLA", "UHC_SHIELD", "HSG", "SKYWARS", "CLASSIC_CRYSTAL"};

    public static ArrayList<ServerPlayer> AXE_QUEUE = new ArrayList<>();
    public static final DuelGameMode AXE = new DuelGameMode("axe", true, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> SWORD_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only", true, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> TRIDENT_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only", true, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> HOE_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode HOE_ONLY = new DuelGameMode("hoe_only", true, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> BOW_ONLY_QUEUE = new ArrayList<>();
    public static final DuelGameMode BOW_ONLY = new DuelGameMode("bow", false, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> VANILLA_QUEUE = new ArrayList<>();
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla", true, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> SHIELD_QUEUE = new ArrayList<>();
    public static final DuelGameMode SHIELD = new DuelGameMode("shield", false, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> NETH_POT_QUEUE = new ArrayList<>();
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot", true, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> POT_QUEUE = new ArrayList<>();
    public static final DuelGameMode POT = new DuelGameMode("neth_pot", true, GameType.ADVENTURE);

    public static ArrayList<ServerPlayer> OG_VANILLA_QUEUE = new ArrayList<>();
    public static final DuelGameMode OG_VANILLA = new DuelGameMode("og_vanilla", true, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> UHC_SHIELD_QUEUE = new ArrayList<>();
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield", false, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> HSG_QUEUE = new ArrayList<>();
    public static final DuelGameMode HSG = new DuelGameMode("hsg", false, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> SKYWARS_QUEUE = new ArrayList<>();
    public static final DuelGameMode SKYWARS = new DuelGameMode("skywars", true, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> CLASSIC_CRYSTAL_QUEUE = new ArrayList<>();
    public static final DuelGameMode CLASSIC_CRYSTAL = new DuelGameMode("classic_crystal", true, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> SMP_QUEUE = new ArrayList<>();
    public static final DuelGameMode SMP = new DuelGameMode("smp", true, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> UHC_QUEUE = new ArrayList<>();
    public static final DuelGameMode UHC = new DuelGameMode("uhc", false, GameType.SURVIVAL);

    public static ArrayList<ServerPlayer> FFA_QUEUE = new ArrayList<>();
    public static final DuelGameMode FFA = new DuelGameMode("ffa", true, GameType.ADVENTURE);

    public static final DuelGameMode LOBBY = new DuelGameMode("lobby", true, GameType.ADVENTURE);

    public static final DuelGameMode SPECTATING = new DuelGameMode("spectating", true, GameType.SPECTATOR);
    public DuelGameMode(String id, boolean hasRegen, GameType gameMode) {
        this.id = id;
        this.hasRegen = hasRegen;
        this.gameMode = gameMode;
    }

    static {
        duelsMaps.add(new ItemStack(Items.SMOOTH_STONE)); // City
        duelsMaps.add(new ItemStack(Items.NETHERITE_BLOCK)); // Neth Flat
        duelsMaps.add(new ItemStack(Items.GRASS_BLOCK)); // Plains
        duelsMaps.add(new ItemStack(Items.ALLIUM)); // Skywars Map (Eden)

        duelsItems.add(new ItemStack(Items.IRON_AXE)); // AXE
        duelsItems.add(new ItemStack(Items.DIAMOND_SWORD)); // SWORD_ONLY
        duelsItems.add(new ItemStack(Items.SHIELD)); // SHIELD
        duelsItems.add(new ItemStack(Items.SPLASH_POTION)); // POT
        duelsItems.add(new ItemStack(Items.LINGERING_POTION)); //NETH_POT
        duelsItems.add(new ItemStack(Items.WATER_BUCKET)); // OG_VANILLA
        duelsItems.add(new ItemStack(Items.NETHERITE_CHESTPLATE)); // SMP
        duelsItems.add(new ItemStack(Items.TRIDENT)); // TRIDENT_ONLY
        duelsItems.add(new ItemStack(Items.NETHERITE_HOE)); // HOE_ONLY
        duelsItems.add(new ItemStack(Items.NETHERITE_SWORD)); // FFA
        duelsItems.add(new ItemStack(Items.BOW)); // BOW_ONLY
        duelsItems.add(new ItemStack(Items.GOLDEN_APPLE)); // UHC
        duelsItems.add(new ItemStack(Items.RESPAWN_ANCHOR)); // VANILLA
        duelsItems.add(new ItemStack(Items.GOLDEN_AXE)); // UHC_SHIELD
        duelsItems.add(new ItemStack(Items.TURTLE_HELMET)); // HSG
        duelsItems.add(new ItemStack(Items.GRASS_BLOCK)); // SKYWARS
        duelsItems.add(new ItemStack(Items.END_CRYSTAL)); // CLASSIC_CRYSTAL
    }
}