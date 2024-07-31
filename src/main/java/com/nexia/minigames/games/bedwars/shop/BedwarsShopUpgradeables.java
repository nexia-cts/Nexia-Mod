package com.nexia.minigames.games.bedwars.shop;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.base.player.NexiaPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Optional;

public class BedwarsShopUpgradeables {

    final static String upgradeableInfoKey = "upgradeableInfo";
    final static String upgradeableIdKey = "id";
    final static String upgradeableLevelKey = "level";

    protected static ItemStack getUpgradeItem(ServerPlayer player, int index) {
        try {
            ItemStack[] upgradeableItems = BedwarsShop.bedWarsUpgradeableItems[index];
            ItemStack comparedStack = upgradeableItems[0].copy();
            int currentLevel;

            currentLevel = getPlayerUpgradeLevel(player, comparedStack);

            if (upgradeableItems.length > currentLevel + 1) {
                return upgradeableItems[currentLevel + 1];
            }
            return upgradeableItems[currentLevel];

        } catch (Exception e) {
            return null;
        }
    }

    private static int getPlayerUpgradeLevel(ServerPlayer player, ItemStack comparedStack) {
        int currentLevel = -1;
        for (ItemStack invItem : ItemStackUtil.getInvItems(player)) {

            if (!isSameUpgradeType(comparedStack, invItem)) continue;
            Optional<Integer> invItemLevel = getItemUpgradeLevel(invItem);
            if (invItemLevel.isPresent() && invItemLevel.get() > currentLevel) {
                currentLevel = invItemLevel.get();
            }

        }
        return currentLevel;
    }

    protected static boolean hasSameUpgradeItem(ServerPlayer player, ItemStack itemStack) {
        for (ItemStack invItem : ItemStackUtil.getInvItems(player)) {
            if (!isSameUpgradeType(invItem, itemStack)) continue;
            Optional<Integer> invLevel = getItemUpgradeLevel(invItem);
            Optional<Integer> itemLevel = getItemUpgradeLevel(itemStack);
            if (invLevel.isPresent() && invLevel.equals(itemLevel)) return true;
        }
        return false;
    }

    protected static boolean isSameUpgradeType(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1.getItem() == Items.AIR || itemStack2.getItem() == Items.AIR) return false;

        String type1 = getItemUpgradeType(itemStack1);
        if (type1 == null) return false;
        String type2 = getItemUpgradeType(itemStack2);
        if (type2 == null) return false;

        return type1.equals(type2);
    }

    protected static String getItemUpgradeType(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return null;
        if (!tag.contains(upgradeableInfoKey) || !(tag.get(upgradeableInfoKey) instanceof CompoundTag)) return null;

        CompoundTag info = (CompoundTag)tag.get(upgradeableInfoKey);
        if (info == null) return null;

        String string = info.getString(upgradeableIdKey);
        if (string.equals("")) return null;
        return string;
    }

    protected static Optional<Integer> getItemUpgradeLevel(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return Optional.empty();
        if (!tag.contains(upgradeableInfoKey) || !(tag.get(upgradeableInfoKey) instanceof CompoundTag)) return Optional.empty();

        CompoundTag info = (CompoundTag)tag.get(upgradeableInfoKey);
        if (info == null) return Optional.empty();
        if (!info.contains(upgradeableLevelKey, 99)) return Optional.empty();
        return Optional.of(info.getInt(upgradeableLevelKey));
    }

    protected static void replaceUpgradeItem(ServerPlayer player, ItemStack newItem, int targetSlot) {
        ArrayList<ItemStack> inventory = ItemStackUtil.getInvItems(player);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack invItem = inventory.get(i);
            if (!isSameUpgradeType(invItem, newItem)) continue;

            Optional<Integer> invLevel = getItemUpgradeLevel(invItem);
            Optional<Integer> newLevel = getItemUpgradeLevel(newItem);

            if (invLevel.isPresent() && newLevel.isPresent() && newLevel.get()-invLevel.get() > 0) {
                player.inventory.setItem(i, newItem);
                return;
            }
        }
        BedwarsShopUtil.giveItem(player, newItem, targetSlot);
    }

    public static ArrayList<ItemStack[]> getActiveUpgradeables() {
        ArrayList<ItemStack[]> activeUpgradeItems = new ArrayList<>();
        for (ItemStack[] itemStacks : BedwarsShop.bedWarsUpgradeableItems) {
            if (itemStacks == null || itemStacks.length < 1) continue;
            activeUpgradeItems.add(itemStacks);
        }
        return activeUpgradeItems;
    }

    public static void downgradePlayerTools(NexiaPlayer player) {
        ArrayList<ItemStack[]> allUpgradeables = getActiveUpgradeables();
        int invSize = player.unwrap().inventory.items.size();

        inv: for (int i = 0; i < invSize; i++) {
            ItemStack invItem = player.unwrap().inventory.getItem(i);

            Optional<Integer> invItemLevel = getItemUpgradeLevel(invItem);
            if (invItemLevel.isEmpty() || invItemLevel.get() < 1) continue;

            for (ItemStack[] upgradeables : allUpgradeables) {
                if (!isSameUpgradeType(invItem, upgradeables[0])) continue;

                ItemStack newItem = upgradeables[invItemLevel.get() - 1].copy();
                BedwarsShopUtil.removeShopNbt(newItem);
                player.unwrap().inventory.setItem(i, newItem);
                continue inv;
            }
        }
    }

}
