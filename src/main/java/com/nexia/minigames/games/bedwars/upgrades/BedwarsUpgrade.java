package com.nexia.minigames.games.bedwars.upgrades;

import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.minigames.games.bedwars.util.BedwarsGen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;

public class BedwarsUpgrade {

    // Nbt key for hashmap keys
    public static final String UPGRADE_TAG_KEY = "upgradeType";

    // Hashmap keys
    public static final String UPGRADE_KEY_SHARPNESS = "sharpness";
    public static final String UPGRADE_KEY_PROTECTION = "protection";
    public static final String UPGRADE_KEY_HASTE = "haste";
    public static final String UPGRADE_KEY_HEALING = "healing";
    public static final String UPGRADE_KEY_GENERATOR = "generator";

    public int level;
    public int[] costs;

    public ItemStack displayItem;
    public int displayRow;
    public int displayColumn;

    private BedwarsUpgrade(int[] costs, int displayRow, int displayColumn, ItemStack displayItem) {
        this.level = 0;
        this.costs = costs;

        this.displayItem = displayItem;
        this.displayRow = displayRow;
        this.displayColumn = displayColumn;
    }

    public static HashMap<String, BedwarsUpgrade> newUpgradeSet() {
        HashMap<String, BedwarsUpgrade> newSet = new HashMap<>();

        newSet.put(UPGRADE_KEY_SHARPNESS, new BedwarsUpgrade(new int[]{4}, 0, 1,
                upgradeItemStack(Items.DIAMOND_SWORD, "Sharpened Blades", "Grants sharpness enchantment\nto all swords and tridents\nfor your team.")));
        newSet.put(UPGRADE_KEY_PROTECTION, new BedwarsUpgrade(new int[]{2, 4, 8, 16}, 0, 2,
                upgradeItemStack(Items.IRON_CHESTPLATE, "Protection", "Grants protection enchantment\nto everyone in your team.")));
        newSet.put(UPGRADE_KEY_HASTE, new BedwarsUpgrade(new int[]{2, 4}, 0, 3,
                upgradeItemStack(Items.GOLDEN_PICKAXE, "Haste", "Grants haste effect to\neveryone in your team.")));
        newSet.put(UPGRADE_KEY_GENERATOR, new BedwarsUpgrade(BedwarsGen.upgradeCosts, 0, 4,
                upgradeItemStack(Items.FURNACE, "Better Generators",
                        null)));
        newSet.put(UPGRADE_KEY_HEALING, new BedwarsUpgrade(new int[]{1}, 0, 5,
                upgradeItemStack(Items.BEACON, "Heal Pool", "Grants regeneration effect to\nevery team member\nat home base.")));

        for (String key : newSet.keySet()) {
            newSet.get(key).displayItem.getOrCreateTag().putString(UPGRADE_TAG_KEY, key);
        }
        return newSet;
    }

    protected static ItemStack upgradeItemStack(Item item, String name, String description) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.setHoverName(new TextComponent("\247r\247f" + name));

        if (description != null) {
            String[] lines = description.split("\n");
            ItemDisplayUtil.addLore(itemStack, " ", -1);
            for (String line : lines) {
                line = "\2477" + line;
                ItemDisplayUtil.addLore(itemStack, line, -1);
            }
            ItemDisplayUtil.addLore(itemStack, " ", -1);
        }

        return itemStack;
    }

}
