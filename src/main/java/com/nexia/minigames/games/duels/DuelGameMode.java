package com.nexia.minigames.games.duels;

import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.nexus.api.world.types.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DuelGameMode {
    public String id;
    public boolean hasRegen;

    public boolean hasSaturation;

    public boolean glint;

    public Minecraft.GameMode gameMode;

    public ItemStack item;

    public ArrayList<NexiaPlayer> queue;

    public static ArrayList<ItemStack> duelsItems = new ArrayList<>();

    public static ArrayList<DuelGameMode> duelGameModes = new ArrayList<>();

    public static ArrayList<String> stringDuelGameModes = new ArrayList<>();



    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only", true, true, Minecraft.GameMode.ADVENTURE, new ItemStack(Items.DIAMOND_SWORD), false);
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only", true, true, Minecraft.GameMode.ADVENTURE, new ItemStack(Items.TRIDENT), false);
    public static final DuelGameMode CLASSIC = new DuelGameMode("classic", true, true, Minecraft.GameMode.ADVENTURE, new ItemStack(Items.DIAMOND_SWORD), true);
    public static final DuelGameMode SHIELD = new DuelGameMode("shield", true, true, Minecraft.GameMode.ADVENTURE, new ItemStack(Items.SHIELD), false);
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield", false, false, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.GOLDEN_APPLE), false);
    public static final DuelGameMode UHC = new DuelGameMode("uhc", false, false, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.LAVA_BUCKET), false);

    public static final DuelGameMode POT = new DuelGameMode("pot", true, false, Minecraft.GameMode.ADVENTURE, new ItemStack(Items.SPLASH_POTION), false);
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot", true, false, Minecraft.GameMode.ADVENTURE, new ItemStack(Items.NETHERITE_SWORD), true);

    public static final DuelGameMode SMP = new DuelGameMode("smp", true, false, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.DIAMOND_CHESTPLATE), true);
    public static final DuelGameMode NETH_SMP = new DuelGameMode("neth_smp", true, false, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.NETHERITE_CHESTPLATE), true);
    public static final DuelGameMode CART = new DuelGameMode("cart", true, false, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.TNT_MINECART), false);

    public static final DuelGameMode DIAMOND_CRYSTAL = new DuelGameMode("diamond_crystal", true, false, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.END_CRYSTAL), false);
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla", true, true, Minecraft.GameMode.SURVIVAL, new ItemStack(Items.RESPAWN_ANCHOR), false);



    public static final DuelGameMode LOBBY = new DuelGameMode("lobby", true, true, Minecraft.GameMode.ADVENTURE, null, false);
    public static final DuelGameMode SPECTATING = new DuelGameMode("spectating", true, true, Minecraft.GameMode.SPECTATOR, null, false);


    public DuelGameMode(String id, boolean hasRegen, boolean hasSaturation, Minecraft.GameMode gameMode, @Nullable ItemStack item, boolean glint) {
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