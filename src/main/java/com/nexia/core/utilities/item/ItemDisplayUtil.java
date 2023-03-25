package com.nexia.core.utilities.item;

import net.minecraft.nbt.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ItemDisplayUtil {

    public static void addLore(ItemStack itemStack, String string, int line) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        CompoundTag display = compoundTag.getCompound("display");
        ListTag listTag = display.getList("Lore", 8);

        string = "{\"text\":\"" + string + "\"}";
        if (line < 0) {
            listTag.add(listTag.size() + line + 1, StringTag.valueOf(string));
        } else {
            listTag.add(line, StringTag.valueOf(string));
        }

        display.put("Lore", listTag);
        compoundTag.put("display", display);
        itemStack.setTag(compoundTag);
    }

    public static void removeLore(ItemStack itemStack, int line) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        CompoundTag display = compoundTag.getCompound("display");
        ListTag listTag = display.getList("Lore", 8);

        if (line < 0) {
            if (listTag.size() > 0) listTag.remove(listTag.size() - 1);
        } else {
            if (listTag.size() > line) {
                listTag.remove(line);
            }
        }

        if (listTag.size() > 0) display.put("Lore", listTag);
        else display.remove("Lore");

        if (display.getAllKeys().size() > 0) compoundTag.put("display", display);
        else compoundTag.remove("display");

        itemStack.setTag(compoundTag);
    }

    public static void addGlint(ItemStack itemStack) {
        try {
            if (!itemStack.isEnchanted()) {
                ListTag listTag = new ListTag();
                listTag.add(new CompoundTag());
                itemStack.getOrCreateTag().put("Enchantments", listTag);
            }
        } catch (Exception ignored) {}
    }

}
