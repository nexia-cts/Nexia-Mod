package com.nexia.ffa.kits;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FfaKit {
    public static ArrayList<FfaKit> ffaKits = new ArrayList<>();

    public static ArrayList<String> stringFfaKits = new ArrayList<>();

    public String id;

    public ItemStack item;

    public static final FfaKit KNIGHT = new FfaKit("Knight", new ItemStack(Items.DIAMOND_SWORD));
    public static final FfaKit POSEIDON = new FfaKit("Poseidon", new ItemStack(Items.TRIDENT));
    public static final FfaKit BRUTE = new FfaKit("Brute", new ItemStack(Items.DIAMOND_AXE));
    public static final FfaKit NINJA = new FfaKit("Ninja", new ItemStack(Items.SUGAR));
    public static final FfaKit VIKING = new FfaKit("Viking", new ItemStack(Items.SHIELD));
    public static final FfaKit ARCHER = new FfaKit("Archer", new ItemStack(Items.BOW));
    public static final FfaKit HUNTER = new FfaKit("Hunter", new ItemStack(Items.CROSSBOW));
    public static final FfaKit FARMER = new FfaKit("Farmer", new ItemStack(Items.DIAMOND_HOE));
    public static final FfaKit RANDOM = new FfaKit("Random", new ItemStack(Items.REDSTONE));

    // Keep track of previously selected kits for each player
    private static Map<ServerPlayer, String> previousKits = new HashMap<>();

    public FfaKit(String id, ItemStack item) {
        this.id = id;
        this.item = item;

        FfaKit.ffaKits.add(this);
        FfaKit.stringFfaKits.add(this.id);
    }

    public static FfaKit identifyKit(String name) {
        for(FfaKit kit : FfaKit.ffaKits) {
            if(kit.id.equalsIgnoreCase(name)) return kit;
        }
        return null;
    }

    public void giveKit(ServerPlayer player, boolean clearEffect) {
        Player fPlayer = PlayerUtil.getFactoryPlayer(player);
        PlayerDataManager.get(player).kit = this;

        if (clearEffect) fPlayer.clearEffects();

        if (this.equals(FfaKit.RANDOM)) {
            ArrayList<String> availableKits = new ArrayList<>(stringFfaKits);
            availableKits.remove(RANDOM.id); // Remove "RANDOM" from the list of available kits

            // Get the previously selected kit for this player
            String previousKit = previousKits.getOrDefault(player, "");

            // Remove the previously selected kit from available kits
            availableKits.remove(previousKit);

            String selectedKit;
            if (!availableKits.isEmpty()) {
                selectedKit = availableKits.get(RandomUtil.randomInt(availableKits.size()));
            } else {
                // If no available kits (all kits were chosen before), select a random one
                selectedKit = stringFfaKits.get(RandomUtil.randomInt(stringFfaKits.size()));
            }

            // Store the selected kit as the previous kit for this player
            previousKits.put(player, selectedKit);

            InventoryUtil.loadInventory(player, "ffa_kits", selectedKit);

            // Announce the kit received to the player
            player.sendMessage(new TextComponent("You received the " + selectedKit + " kit!"), player.getUUID());
        } else {
            InventoryUtil.loadInventory(player, "ffa_kits", this.id);

            // Announce the kit received to the player
            player.sendMessage(new TextComponent("You received the " + this.id + " kit!"), player.getUUID());
        }
    }
}