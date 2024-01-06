package com.nexia.minigames.games.duels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;

public class DuelGameMode {
    public String id;
    public boolean hasRegen;

    public boolean hasSaturation;

    public GameType gameMode;

    public ArrayList<ServerPlayer> queue;

    public static ArrayList<ItemStack> duelsItems = new ArrayList<>();

    public static ArrayList<DuelGameMode> duelGameModes = new ArrayList<>();
    public static String[] duels = {"AXE", "SWORD_ONLY", "SHIELD", "POT", "NETH_POT", "OG_VANILLA", "SMP", "TRIDENT_ONLY", "HOE_ONLY", "FFA", "BOW_ONLY", "UHC", "VANILLA", "UHC_SHIELD", "HSG", "SKYWARS", "CLASSIC_CRYSTAL"};

    public static final DuelGameMode AXE = new DuelGameMode("axe", true, true, GameType.ADVENTURE);
    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only", true, true, GameType.ADVENTURE);
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only", true, true, GameType.ADVENTURE);
    public static final DuelGameMode HOE_ONLY = new DuelGameMode("hoe_only", true, true, GameType.ADVENTURE);
    public static final DuelGameMode BOW_ONLY = new DuelGameMode("bow", false, true, GameType.ADVENTURE);
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla", true, true, GameType.SURVIVAL);
    public static final DuelGameMode SHIELD = new DuelGameMode("shield", false, true, GameType.ADVENTURE);
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot", true, true, GameType.ADVENTURE);
    public static final DuelGameMode POT = new DuelGameMode("neth_pot", true, true, GameType.ADVENTURE);
    public static final DuelGameMode OG_VANILLA = new DuelGameMode("og_vanilla", true, true, GameType.SURVIVAL);
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield", false, true, GameType.SURVIVAL);
    public static final DuelGameMode HSG = new DuelGameMode("hsg", false, true, GameType.SURVIVAL);
    public static final DuelGameMode SKYWARS = new DuelGameMode("skywars", true, true, GameType.SURVIVAL);
    public static final DuelGameMode CLASSIC_CRYSTAL = new DuelGameMode("classic_crystal", true, true, GameType.SURVIVAL);
    public static final DuelGameMode SMP = new DuelGameMode("smp", true, true, GameType.SURVIVAL);
    public static final DuelGameMode UHC = new DuelGameMode("uhc", false, true, GameType.SURVIVAL);

    public static final DuelGameMode FFA = new DuelGameMode("ffa", true, true, GameType.ADVENTURE);



    public static final DuelGameMode LOBBY = new DuelGameMode("lobby", true, true, GameType.ADVENTURE);
    public static final DuelGameMode SPECTATING = new DuelGameMode("spectating", true, true, GameType.SPECTATOR);
    public DuelGameMode(String id, boolean hasRegen, boolean hasSaturation, GameType gameMode) {
        this.id = id;
        this.hasRegen = hasRegen;
        this.hasSaturation = hasSaturation;
        this.gameMode = gameMode;
        this.queue = new ArrayList<>();

        DuelGameMode.duelGameModes.add(this);
    }

    static {
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