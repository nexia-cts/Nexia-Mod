package com.nexia.minigames.games.duels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class DuelGameMode {
    String id;

    public static ArrayList<ItemStack> duelsMaps = new ArrayList<>();

    public static ArrayList<ItemStack> duelsItems = new ArrayList<>();
    public static String[] duels = {"AXE", "SWORD_ONLY", "SHIELD", "POT", "NETH_POT", "OG_VANILLA", "SMP", "TRIDENT_ONLY", "HOE_ONLY", "FFA", "BOW_ONLY", "UHC", "VANILLA", "UHC_SHIELD"};

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

    public static ArrayList<ServerPlayer> SHIELD_QUEUE = new ArrayList<>();
    public static final DuelGameMode SHIELD = new DuelGameMode("shield");

    public static ArrayList<ServerPlayer> NETH_POT_QUEUE = new ArrayList<>();
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot");

    public static ArrayList<ServerPlayer> POT_QUEUE = new ArrayList<>();
    public static final DuelGameMode POT = new DuelGameMode("neth_pot");

    public static ArrayList<ServerPlayer> OG_VANILLA_QUEUE = new ArrayList<>();
    public static final DuelGameMode OG_VANILLA = new DuelGameMode("og_vanilla");

    public static ArrayList<ServerPlayer> UHC_SHIELD_QUEUE = new ArrayList<>();
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield");

    public static ArrayList<ServerPlayer> SMP_QUEUE = new ArrayList<>();
    public static final DuelGameMode SMP = new DuelGameMode("smp");

    public static ArrayList<ServerPlayer> UHC_QUEUE = new ArrayList<>();
    public static final DuelGameMode UHC = new DuelGameMode("uhc");

    public static ArrayList<ServerPlayer> FFA_QUEUE = new ArrayList<>();
    public static final DuelGameMode FFA = new DuelGameMode("ffa");

    public static final DuelGameMode LOBBY = new DuelGameMode("lobby");
    public DuelGameMode(String id) {
        this.id = id;

        duelsMaps.add(new ItemStack(Items.SMOOTH_STONE)); // City
        duelsMaps.add(new ItemStack(Items.NETHERITE_BLOCK)); // Neth Flat
        duelsMaps.add(new ItemStack(Items.GRASS_BLOCK)); // Plains
        duelsMaps.add(new ItemStack(Items.SAND)); // Desert
        duelsMaps.add(new ItemStack(Items.COBWEB)); // Sky

        duelsItems.add(new ItemStack(Items.DIAMOND_AXE)); // AXE
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
        duelsItems.add(new ItemStack(Items.END_CRYSTAL)); // VANILLA
        duelsItems.add(new ItemStack(Items.GOLDEN_AXE)); // UHC_SHIELD
    }
}
