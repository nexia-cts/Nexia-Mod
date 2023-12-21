package com.nexia.minigames.games.duels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DuelGameMode {
    public String id;
    public boolean hasRegen;

    public boolean hasSaturation;

    public GameType gameMode;

    public ItemStack item;

    public ArrayList<ServerPlayer> queue;

    public static ArrayList<ItemStack> duelsItems = new ArrayList<>();

    public static ArrayList<DuelGameMode> duelGameModes = new ArrayList<>();

    public static ArrayList<String> stringDuelGameModes = new ArrayList<>();


    public static final DuelGameMode CLASSIC = new DuelGameMode("classic", true, true, GameType.ADVENTURE, new ItemStack(Items.NETHERITE_SWORD));
    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only", true, true, GameType.ADVENTURE, new ItemStack(Items.DIAMOND_SWORD));
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only", true, true, GameType.ADVENTURE, new ItemStack(Items.TRIDENT));
    public static final DuelGameMode HOE_ONLY = new DuelGameMode("hoe_only", true, true, GameType.ADVENTURE, new ItemStack(Items.NETHERITE_HOE));
    public static final DuelGameMode BOW_ONLY = new DuelGameMode("bow_only", false, true, GameType.ADVENTURE, new ItemStack(Items.BOW));
    public static final DuelGameMode AXE = new DuelGameMode("axe", true, true, GameType.ADVENTURE, new ItemStack(Items.DIAMOND_AXE));
    public static final DuelGameMode SHIELD = new DuelGameMode("shield", false, true, GameType.ADVENTURE, new ItemStack(Items.SHIELD));
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield", false, true, GameType.SURVIVAL, new ItemStack(Items.LAVA_BUCKET));
    public static final DuelGameMode UHC = new DuelGameMode("uhc", false, true, GameType.SURVIVAL, new ItemStack(Items.GOLDEN_APPLE));
    public static final DuelGameMode SKYWARS = new DuelGameMode("skywars", true, true, GameType.SURVIVAL, new ItemStack(Items.GRASS_BLOCK));
    public static final DuelGameMode HSG = new DuelGameMode("hsg", false, true, GameType.SURVIVAL, new ItemStack(Items.TURTLE_HELMET));
    public static final DuelGameMode POT = new DuelGameMode("pot", true, true, GameType.ADVENTURE, new ItemStack(Items.SPLASH_POTION));
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot", true, true, GameType.ADVENTURE, new ItemStack(Items.LINGERING_POTION));
    public static final DuelGameMode SMP = new DuelGameMode("smp", true, true, GameType.SURVIVAL, new ItemStack(Items.NETHERITE_CHESTPLATE));
    public static final DuelGameMode CLASSIC_CRYSTAL = new DuelGameMode("classic_crystal", true, true, GameType.SURVIVAL, new ItemStack(Items.END_CRYSTAL));
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla", true, true, GameType.SURVIVAL, new ItemStack(Items.RESPAWN_ANCHOR));



    public static final DuelGameMode LOBBY = new DuelGameMode("lobby", true, true, GameType.ADVENTURE, null);
    public static final DuelGameMode SPECTATING = new DuelGameMode("spectating", true, true, GameType.SPECTATOR, null);


    public DuelGameMode(String id, boolean hasRegen, boolean hasSaturation, GameType gameMode, @Nullable ItemStack item) {
        this.id = id;
        this.hasRegen = hasRegen;
        this.hasSaturation = hasSaturation;
        this.gameMode = gameMode;
        this.queue = new ArrayList<>();
        this.item = item;

        if(item != null) {
            DuelGameMode.duelsItems.add(item);
            DuelGameMode.duelGameModes.add(this);
            DuelGameMode.stringDuelGameModes.add(id.toUpperCase());
        }
    }
}