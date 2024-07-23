package com.nexia.ffa.kits;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.NexiaFfa;
import com.nexia.ffa.kits.utilities.player.KitFFAPlayerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;

public class FfaKit {
    public static ArrayList<FfaKit> ffaKits = new ArrayList<>();

    public static ArrayList<String> stringFfaKits = new ArrayList<>();

    public String id;

    public ItemStack item;

    public static final FfaKit KNIGHT = new FfaKit("knight", new ItemStack(Items.DIAMOND_SWORD));
    public static final FfaKit POSEIDON = new FfaKit("poseidon", new ItemStack(Items.TRIDENT));
    public static final FfaKit BRUTE = new FfaKit("nrute", new ItemStack(Items.DIAMOND_AXE));
    public static final FfaKit NINJA = new FfaKit("ninja", new ItemStack(Items.SUGAR));
    public static final FfaKit VIKING = new FfaKit("viking", new ItemStack(Items.SHIELD));
    public static final FfaKit ARCHER = new FfaKit("archer", new ItemStack(Items.BOW));
    public static final FfaKit HUNTER = new FfaKit("hunter", new ItemStack(Items.CROSSBOW));
    public static final FfaKit FARMER = new FfaKit("farmer", new ItemStack(Items.DIAMOND_HOE));
    public static final FfaKit RANDOM = new FfaKit("random", new ItemStack(Items.REDSTONE));

    // Keep track of previously selected kits for each player
    private static final HashMap<NexiaPlayer, String> previousKits = new HashMap<>();

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

    public void giveKit(NexiaPlayer player, boolean clearEffect) {
        ((KitFFAPlayerData)PlayerDataManager.getDataManager(NexiaFfa.FFA_KITS_DATA_MANAGER).get(player)).kit = this;

        if (clearEffect) player.clearEffects();

        if (this.equals(FfaKit.RANDOM)) {
            ArrayList<String> availableKits = new ArrayList<>(stringFfaKits);
            availableKits.remove(RANDOM.id);

            String previousKit = previousKits.getOrDefault(player, "");
            availableKits.remove(previousKit);

            String selectedKit;
            if (!availableKits.isEmpty()) {
                selectedKit = availableKits.get(RandomUtil.randomInt(availableKits.size()));
            } else {
                selectedKit = stringFfaKits.get(RandomUtil.randomInt(stringFfaKits.size()));
            }

            // Store the selected kit as the previous kit for this player
            previousKits.put(player, selectedKit);
            InventoryUtil.loadInventory(player, "ffa_kits", selectedKit);
        } else {
            InventoryUtil.loadInventory(player, "ffa_kits", this.id);
        }
    }
}