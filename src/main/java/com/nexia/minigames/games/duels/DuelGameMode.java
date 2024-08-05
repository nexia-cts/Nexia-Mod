package com.nexia.minigames.games.duels;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import net.kyori.adventure.text.Component;
import net.minecraft.world.item.Item;
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


    public static final DuelGameMode SWORD_ONLY = new DuelGameMode("sword_only", true, true, Minecraft.GameMode.ADVENTURE, Items.DIAMOND_SWORD, false);
    public static final DuelGameMode TRIDENT_ONLY = new DuelGameMode("trident_only", true, true, Minecraft.GameMode.ADVENTURE, Items.TRIDENT, false);
    public static final DuelGameMode CLASSIC = new DuelGameMode("classic", true, true, Minecraft.GameMode.ADVENTURE, Items.DIAMOND_SWORD, true);
    public static final DuelGameMode SHIELD = new DuelGameMode("shield", true, true, Minecraft.GameMode.ADVENTURE, Items.SHIELD, false);
    public static final DuelGameMode UHC_SHIELD = new DuelGameMode("uhc_shield", false, false, Minecraft.GameMode.SURVIVAL, Items.GOLDEN_APPLE, false);
    public static final DuelGameMode UHC = new DuelGameMode("uhc", false, false, Minecraft.GameMode.SURVIVAL, Items.LAVA_BUCKET, false);

    public static final DuelGameMode POT = new DuelGameMode("pot", true, false, Minecraft.GameMode.ADVENTURE, Items.SPLASH_POTION, false);
    public static final DuelGameMode NETH_POT = new DuelGameMode("neth_pot", true, false, Minecraft.GameMode.ADVENTURE, Items.NETHERITE_SWORD, true);

    public static final DuelGameMode SMP = new DuelGameMode("smp", true, false, Minecraft.GameMode.SURVIVAL, Items.DIAMOND_CHESTPLATE, true);
    public static final DuelGameMode NETH_SMP = new DuelGameMode("neth_smp", true, false, Minecraft.GameMode.SURVIVAL, Items.NETHERITE_CHESTPLATE, true);
    public static final DuelGameMode CART = new DuelGameMode("cart", true, false, Minecraft.GameMode.SURVIVAL, Items.TNT_MINECART, false);

    public static final DuelGameMode DIAMOND_CRYSTAL = new DuelGameMode("diamond_crystal", true, false, Minecraft.GameMode.SURVIVAL, Items.END_CRYSTAL, false);
    public static final DuelGameMode VANILLA = new DuelGameMode("vanilla", true, false, Minecraft.GameMode.SURVIVAL, Items.RESPAWN_ANCHOR, false);



    public static final DuelGameMode LOBBY = new DuelGameMode("lobby", true, true, Minecraft.GameMode.ADVENTURE, null, false);
    public static final DuelGameMode SPECTATING = new DuelGameMode("spectating", true, true, Minecraft.GameMode.SPECTATOR, null, false);


    public DuelGameMode(String id, boolean hasRegen, boolean hasSaturation, Minecraft.GameMode gameMode, @Nullable Item item, boolean glint) {
        this.id = id;
        this.hasRegen = hasRegen;
        this.hasSaturation = hasSaturation;
        this.gameMode = gameMode;
        this.queue = new ArrayList<>();
        this.glint = glint;

        if(item != null) {
            ItemStack itemStack = new ItemStack(item);
            if(glint) ItemDisplayUtil.addGlint(itemStack);

            itemStack.setHoverName(ObjectMappings.convertComponent(Component.text(id.toUpperCase().replaceAll("_", " "), ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false)));
            ItemDisplayUtil.removeLore(itemStack, 0);
            ItemDisplayUtil.removeLore(itemStack, 1);
            itemStack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
            itemStack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);

            this.item = itemStack;
            DuelGameMode.duelsItems.add(itemStack);

            DuelGameMode.duelGameModes.add(this);
            DuelGameMode.stringDuelGameModes.add(id.toUpperCase());
        }
    }
}