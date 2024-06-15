package com.nexia.ffa.kits;

import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class FfaKit {
    public static ArrayList<FfaKit> ffaKits = new ArrayList<>();

    public static ArrayList<String> stringFfaKits = new ArrayList<>();

    public String id;

    public ItemStack item;

    public static final FfaKit KNIGHT = new FfaKit("knight", new ItemStack(Items.DIAMOND_SWORD));
    public static final FfaKit POSEIDON = new FfaKit("poseidon", new ItemStack(Items.TRIDENT));
    public static final FfaKit BRUTE = new FfaKit("brute", new ItemStack(Items.NETHERITE_AXE));
    public static final FfaKit HUNTER = new FfaKit("hunter", new ItemStack(Items.CROSSBOW));
    public static final FfaKit NINJA = new FfaKit("ninja", new ItemStack(Items.SUGAR));
    public static final FfaKit REAPER = new FfaKit("reaper", new ItemStack(Items.NETHERITE_HOE));
    public static final FfaKit RANDOM = new FfaKit("random", new ItemStack(Items.BARRIER));

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
        PlayerDataManager.get(player).kit = this;

        if (clearEffect) player.clearEffects();

        if (this.equals(FfaKit.RANDOM)) {
            ArrayList<String> availableKits = new ArrayList<>(stringFfaKits);
            availableKits.remove(FfaKit.RANDOM.id);
            String selectedKit = availableKits.get(RandomUtil.randomInt(availableKits.size()));
            InventoryUtil.loadInventory(player, "ffa_kits", selectedKit);
        } else {
            InventoryUtil.loadInventory(player, "ffa_kits", this.id);
        }
    }
}