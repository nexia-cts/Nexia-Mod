package com.nexia.minigames.games.duels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.notcoded.codelib.util.item.ItemDisplayUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DuelGameMode {
    public String id;
    public boolean hasRegen;

    public boolean hasSaturation;

    public boolean glint;

    public GameType gameMode;

    public ItemStack item;

    public ArrayList<ServerPlayer> queue;

    public static ArrayList<ItemStack> duelsItems = new ArrayList<>();

    public static ArrayList<DuelGameMode> duelGameModes = new ArrayList<>();

    public static ArrayList<String> stringDuelGameModes = new ArrayList<>();



    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only", true, true, GameType.ADVENTURE, new ItemStack(Items.DIAMOND_SWORD), false);
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only", true, true, GameType.ADVENTURE, new ItemStack(Items.TRIDENT), false);
    public static final DuelGameMode CLASSIC = new DuelGameMode("classic", true, true, GameType.ADVENTURE, new ItemStack(Items.DIAMOND_SWORD), true);
    public static final DuelGameMode SHIELD = new DuelGameMode("shield", true, false, GameType.ADVENTURE, new ItemStack(Items.SHIELD), false);
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield", false, false, GameType.SURVIVAL, new ItemStack(Items.GOLDEN_APPLE), false);
    public static final DuelGameMode UHC = new DuelGameMode("uhc", false, false, GameType.SURVIVAL, new ItemStack(Items.LAVA_BUCKET), false);

    public static final DuelGameMode POT = new DuelGameMode("pot", true, false, GameType.ADVENTURE, new ItemStack(Items.SPLASH_POTION), false);
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot", true, false, GameType.ADVENTURE, new ItemStack(Items.NETHERITE_SWORD), true);

    public static final DuelGameMode OG_VANILLA = new DuelGameMode("og_vanilla", true, false, GameType.SURVIVAL, new ItemStack(Items.DIAMOND_CHESTPLATE), true);
    public static final DuelGameMode NETH_SMP = new DuelGameMode("neth_smp", true, false, GameType.SURVIVAL, new ItemStack(Items.NETHERITE_CHESTPLATE), true);
    public static final DuelGameMode CART = new DuelGameMode("cart", true, false, GameType.SURVIVAL, new ItemStack(Items.TNT_MINECART), false);

    public static final DuelGameMode DIAMOND_CRYSTAL = new DuelGameMode("diamond_crystal", true, false, GameType.SURVIVAL, new ItemStack(Items.END_CRYSTAL), false);
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla", true, true, GameType.SURVIVAL, new ItemStack(Items.RESPAWN_ANCHOR), false);



    public static final DuelGameMode LOBBY = new DuelGameMode("lobby", true, true, GameType.ADVENTURE, null, false);
    public static final DuelGameMode SPECTATING = new DuelGameMode("spectating", true, true, GameType.SPECTATOR, null, false);


    public DuelGameMode(String id, boolean hasRegen, boolean hasSaturation, GameType gameMode, @Nullable ItemStack item, boolean glint) {
        this.id = id;
        this.hasRegen = hasRegen;
        this.hasSaturation = hasSaturation;
        this.gameMode = gameMode;
        this.queue = new ArrayList<>();
        this.glint = glint;
        this.item = item;

        if(item != null) {

            if(glint) ItemDisplayUtil.addGlint(item);
            DuelGameMode.duelsItems.add(item);

            DuelGameMode.duelGameModes.add(this);
            DuelGameMode.stringDuelGameModes.add(id.toUpperCase());
        }
    }
}