package com.nexia.minigames.games.bedwars.shop;

import com.nexia.core.utilities.item.ItemDisplayUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BwShopCost {

    final static String costFormat = "\247r\2477Cost: ";

    static void addCostLore(ItemStack itemStack, ItemStack cost) {
        String color = "\247f";
        if (cost.getItem() == Items.IRON_INGOT) color = "\247f";
        if (cost.getItem() == Items.GOLD_INGOT) color = "\2476";
        if (cost.getItem() == Items.DIAMOND) color = "\247b";
        if (cost.getItem() == Items.EMERALD) color = "\2472";

        String name = cost.getItem().toString();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        if (cost.getCount() != 1) name += "s";

        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '_') {
                name = name.substring(0, i) + ' ' + name.substring(i+1);
                if (name.length() > i+1) {
                    name = name.substring(0, i+1) + name.substring(i+1, i+2).toUpperCase() + name.substring(i+2);
                }
            }
        }
        name = costFormat + color + cost.getCount() + " " + name;

        ItemDisplayUtil.addLore(itemStack, name, 0);
    }

    protected static void removeCostLore(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = compoundTag.getCompound("display").getList("Lore", 8);
        if (listTag != null && !listTag.isEmpty() && listTag.get(0).toString().contains(costFormat)) {
            ItemDisplayUtil.removeLore(itemStack, 0);
        }
    }

}
