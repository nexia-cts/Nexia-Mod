package com.nexia.core.utilities.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public class ItemDisplayUtil {

    public static ItemStack addLore(ItemStack itemStack, String string, int line) {
        return addUnformattedLore(itemStack, "{\"text\":\"" + string + "\"}", line);
    }

    private static ItemStack addUnformattedLore(ItemStack itemStack, String string, int line) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        CompoundTag display = compoundTag.getCompound("display");
        ListTag listTag = display.getList("Lore", 8);

        if (line < 0) {
            listTag.add(listTag.size() + line + 1, StringTag.valueOf(string));
        } else {
            listTag.add(line, StringTag.valueOf(string));
        }

        display.put("Lore", listTag);
        compoundTag.put("display", display);
        itemStack.setTag(compoundTag);

        return itemStack;
    }

    public static ItemStack addLore(ItemStack itemStack, Component component, int line) {
        return addUnformattedLore(itemStack, GsonComponentSerializer.gson().serialize(component), line);
    }

    public static void removeLore(ItemStack itemStack, int line) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        CompoundTag display = compoundTag.getCompound("display");
        ListTag listTag = display.getList("Lore", 8);

        if (line < 0) {
            if (!listTag.isEmpty()) listTag.removeLast();
        } else {
            if (listTag.size() > line) {
                listTag.remove(line);
            }
        }

        if (!listTag.isEmpty()) display.put("Lore", listTag);
        else display.remove("Lore");

        if (!display.getAllKeys().isEmpty()) compoundTag.put("display", display);
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
