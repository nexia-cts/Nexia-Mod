package com.nexia.minigames.games.bedwars.shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.minigames.games.bedwars.BedwarsGame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.FileReader;
import java.nio.file.Path;

public class BedwarsLoadShop {

    final static String shopFileName = "shop.json";

    final static String itemsListKey = "items";
    final static String typeKey = "type";
    final static String upgradeableTypeKey = "upgradeabletype";

    final static String upgradeableItemsKey = "items";

    final static String slotKey = "slot";
    final static String itemKey = "item";
    final static String costItemKey = "costitem";
    final static String nameKey = "name";
    final static String descriptionKey = "description";
    final static String nbtKey = "nbt";

    public static boolean loadBedWarsShop(boolean useDefaultOnFailure) {
        JsonArray jsonArray = readBedWarsShopFile();

        if (jsonArray == null) {
            if (useDefaultOnFailure) {
                System.out.println("Failed to read BedWars shop file, using villagers");
            } else {
                System.out.println("Failed to read BedWars shop file");
            }
            return false;
        }

        loadShopFromJson(jsonArray);
        return true;
    }

    static JsonArray readBedWarsShopFile() {
        try {
            Path path = Path.of(BedwarsGame.bedWarsDirectory + "/" + shopFileName);
            if (!path.toFile().exists()) return null;

            JsonReader jsonReader = new JsonReader(new FileReader(path.toFile()));
            JsonParser jsonParser = new JsonParser();
            JsonElement element = jsonParser.parse(jsonReader);
            jsonReader.close();

            if (!element.isJsonObject()) return null;
            JsonObject object = (JsonObject) element;

            JsonElement kitElement = object.get(itemsListKey);
            if (kitElement == null || !kitElement.isJsonArray()) return null;
            return (JsonArray) kitElement;
        } catch (Exception e) {
            return null;
        }
    }

    static void loadShopFromJson(JsonArray jsonArray) {
        BedwarsShop.bedWarsShopItems = new ItemStack[BedwarsShop.shopSize];
        BedwarsShop.bedWarsUpgradeableItems = new ItemStack[28][];

        for (JsonElement element : jsonArray) {
            try {
                JsonObject object = (JsonObject) element;

                int slot = object.get(slotKey).getAsInt();
                if (slot >= BedwarsShop.shopSize || slot < 0) continue;

                String type = "default";
                if (object.has(typeKey)) type = object.get(typeKey).getAsString();

                if (type.equals("upgradeable")) {
                    ItemStack[] upgradeableItems = getUpgradeableItems(object);
                    if (upgradeableItems != null) BedwarsShop.bedWarsUpgradeableItems[slot] = upgradeableItems;
                } else {
                    ItemStack itemStack = getItemStackFromJson(object);
                    if (itemStack != null) BedwarsShop.bedWarsShopItems[slot] = itemStack;
                }

            } catch (Exception e) {
                System.out.println("Could not load item in BedWars shop");
            }
        }
    }

    static ItemStack[] getUpgradeableItems(JsonObject object) {
        JsonArray upgradeableList = object.get(upgradeableItemsKey).getAsJsonArray();
        String upgradeableItemId = object.get(upgradeableTypeKey).getAsString();

        int size = upgradeableList.size();
        if (size < 1) return null;

        ItemStack[] upgradeableItems = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            ItemStack upgradeableItem = getItemStackFromJson(upgradeableList.get(i).getAsJsonObject());
            if (upgradeableItem == null) continue;

            CompoundTag info = new CompoundTag();
            info.putString(BedwarsShopUpgradeables.upgradeableIdKey, upgradeableItemId);
            info.putInt(BedwarsShopUpgradeables.upgradeableLevelKey, i);
            upgradeableItem.getOrCreateTag().put(BedwarsShopUpgradeables.upgradeableInfoKey, info);

            upgradeableItems[i] = upgradeableItem;
        }
        return upgradeableItems;
    }

    static ItemStack getItemStackFromJson(JsonObject object) {

        ItemStack itemStack = ItemStackUtil.stringToItemStack(object.get(itemKey).getAsString());
        ItemStack costStack = ItemStackUtil.stringToItemStack(object.get(costItemKey).getAsString());

        String nbt = "{}";
        if (object.has(nbtKey)) nbt = object.get(nbtKey).getAsString();
        String name = null;
        if (object.has(nameKey)) name = object.get(nameKey).getAsString();
        String description = null;
        if (object.has(descriptionKey)) description = object.get(descriptionKey).getAsString();

        return turnIntoBedWarsItem(itemStack, costStack, name, description, nbt);

    }

    static ItemStack turnIntoBedWarsItem(ItemStack itemStack, ItemStack costStack, String name, String description, String nbt) {
        try {
            if (itemStack.getItem() == null || itemStack.getItem() == Items.AIR) return null;
            if (costStack.getItem() == null || costStack.getItem() == Items.AIR) return null;

            CompoundTag compoundTag = itemStack.getOrCreateTag();
            compoundTag.putBoolean(BedwarsShop.purchasableKey, true);
            compoundTag.putString(BedwarsShop.currencyItemKey, costStack.getItem().toString());
            compoundTag.putInt(BedwarsShop.currencyAmountKey, costStack.getCount());

            compoundTag.merge(TagParser.parseTag(nbt));
            if (itemStack.getMaxDamage() != 0) compoundTag.putInt("Unbreakable", 1);

            if (description != null) addDescriptionLore(itemStack, description);
            if (name != null) itemStack.setHoverName(new TextComponent("\247f" + name));

            return itemStack;
        } catch (Exception e) {
            return null;
        }
    }

    static void addDescriptionLore(ItemStack itemStack, String description) {
        String[] lines = description.split("\\\\n");

        ItemDisplayUtil.addLore(itemStack, " ", -1);
        for (String line : lines) {
            line = "\2477" + line;
            ItemDisplayUtil.addLore(itemStack, line, -1);
        }
        ItemDisplayUtil.addLore(itemStack, " ", -1);
    }

}
