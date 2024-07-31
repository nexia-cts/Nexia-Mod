package com.nexia.minigames.games.bedwars.shop;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BwShopUtil {

    protected static ItemStack getShopItem(ServerPlayer player, int index) {
        ItemStack itemStack = BwShop.bedWarsShopItems[index];

        if (itemStack != null) {
            return itemStack;
        } else {
            return BwShopUpgradeables.getUpgradeItem(player, index);
        }
    }

    protected static ItemStack toGuiItem(ItemStack itemStack) {
        itemStack = itemStack.copy();
        ItemStack cost = getCost(itemStack);
        if (cost == null) return null;
        BwShopCost.addCostLore(itemStack, cost);
        return itemStack;
    }

    protected static void removeShopNbt(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return;

        BwShopCost.removeCostLore(itemStack);
        tag.remove(BwShop.purchasableKey);
        tag.remove(BwShop.currencyItemKey);
        tag.remove(BwShop.currencyAmountKey);

        if (tag.getAsString().equals("{}")) itemStack.setTag(null);
        else itemStack.setTag(tag);
    }

    protected static ItemStack getCost(ItemStack soldItem) {
        try {
            CompoundTag compoundTag = soldItem.getOrCreateTag();
            Item item = Registry.ITEM.get(new ResourceLocation(compoundTag.getString(BwShop.currencyItemKey)));
            int amount = compoundTag.getInt(BwShop.currencyAmountKey);
            return new ItemStack(item, amount);
        } catch (Exception e) {
            return null;
        }
    }

    protected static void giveItem(ServerPlayer player, ItemStack itemStack, int targetSlot) {
        itemStack = itemStack.copy();

        // Try to set item into hotkeyed slot
        if (targetSlot >= 0 && targetSlot < 36) {
            ItemStack invItem = player.inventory.getItem(targetSlot);
            if (combineIfSameItem(itemStack, invItem)) {
                if (itemStack.getCount() == 0) return;
            } else {
                player.inventory.setItem(targetSlot, itemStack);
                player.inventory.add(invItem);
                return;
            }
        }

        // Try to combine item with existing items of same type in inventory
        for (int i = 0; i < player.inventory.items.size() + 1; i++) {
            int slot = i;
            if (i == 36) slot = 40;
            ItemStack invItem = player.inventory.getItem(slot);
            combineIfSameItem(itemStack, invItem);
            if (itemStack.getCount() == 0) return;
        }

        // Normal give except moves resource items out of the way
        for (int i = 0; i < player.inventory.items.size() + 1; i++) {
            int slot = i;
            if (i == 36) slot = 40;
            ItemStack invItem = player.inventory.getItem(slot);
            if (invItem.isEmpty()) {
                player.inventory.setItem(slot, itemStack);
                return;
            } else if (BwUtil.isBedWarsCurrency(invItem)) {
                player.inventory.setItem(slot, itemStack);
                player.inventory.add(invItem);
                return;
            }
        }
    }

    private static boolean combineIfSameItem(ItemStack itemStack, ItemStack invItem) {
        if (!ItemStackUtil.isSameItem(itemStack, invItem)) return false;

        int availableSpace = invItem.getMaxStackSize() - invItem.getCount();
        if (availableSpace >= itemStack.getCount()) {
            invItem.setCount(invItem.getCount() + itemStack.getCount());
            itemStack.setCount(0);
        } else {
            invItem.setCount(invItem.getMaxStackSize());
            itemStack.shrink(availableSpace);
        }
        return true;
    }

    protected static void giveSword(ServerPlayer player, ItemStack soldItem) {
        Inventory inv = player.inventory;

        for (int i = 0; i < inv.items.size(); i++) {
            if (BwUtil.isDefaultSword(inv.getItem(i))) {
                inv.setItem(i, soldItem);
                return;
            }
        }
        giveItem(player, soldItem, -1);
    }

    protected static void giveArmorItems(Player player, ItemStack armorPiece) {
        String type = armorPiece.getItem().toString().split("_")[0];

        String bootsItemName = type + "_boots";
        player.inventory.setItem(36, armorItemFromName(bootsItemName));

        String leggingsItemName = type + "_leggings";
        player.inventory.setItem(37, armorItemFromName(leggingsItemName));
    }

    private static ItemStack armorItemFromName(String itemName) {
        Item bootsItem = ItemStackUtil.itemFromString(itemName);
        ItemStack boots = new ItemStack(bootsItem);
        boots.getOrCreateTag().putInt("Unbreakable", 1);
        return boots;
    }

    protected static ItemStack setBlockColor(ServerPlayer player, ItemStack original) {
        try {
            String color = BwTeam.getPlayerTeamColor(new NexiaPlayer(player));
            Item item = original.getItem();

            if (original.getItem() == Items.WHITE_WOOL) {
                assert color != null;
                item = Registry.ITEM.get(new ResourceLocation(item.toString().replace("white", color)));
            } else if (original.getItem() == Items.GLASS) {
                item = Registry.ITEM.get(new ResourceLocation(color + "_stained_glass"));
            } else {
                return original;
            }

            if (item == Items.AIR) return original;

            ItemStack newItem = new ItemStack(item, original.getCount());
            if (original.hasTag()) newItem.setTag(original.getTag());
            return newItem;

        } catch (Exception e) {
            return original;
        }
    }

}
