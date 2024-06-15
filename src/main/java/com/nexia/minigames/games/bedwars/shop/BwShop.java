package com.nexia.minigames.games.bedwars.shop;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class BwShop extends SimpleGui {

    public static ItemStack[] bedWarsShopItems = null;
    public static ItemStack[][] bedWarsUpgradeableItems = null;
    final static int shopHeight = 4;
    final static int shopLength = 7;
    final static int shopSize = shopHeight * shopLength;

    final static String purchasableKey = "bedWarsPurchasable";
    final static TextComponent title = new TextComponent("Item Shop");

    final static String currencyItemKey = "bwCurrencyItem";
    final static String currencyAmountKey = "bwCurrencyAmount";

    public BwShop(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    public static void openShopGui(ServerPlayer player) {
        BwShop shop = new BwShop(MenuType.GENERIC_9x6, player, false);
        shop.setTitle(title);
        for (int i = 0; i < bedWarsShopItems.length; i++) {
            ItemStack itemStack = BwShopUtil.getShopItem(player, i);
            if (itemStack == null) continue;
            itemStack = BwShopUtil.toGuiItem(itemStack);
            if (itemStack == null) continue;

            shop.setSlot(indexToGuiSlot(i), itemStack);
        }
        shop.open();
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            ItemStack itemStack = element.getItemStack();
            CompoundTag tag = itemStack.getOrCreateTag();

            int targetSlot = -1;
            if (clickType.numKey) targetSlot = clickType.value - 1;

            if (tag.contains(purchasableKey) && tag.getBoolean(purchasableKey) && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
                this.tryToPurchase(player, itemStack.copy(), index, targetSlot);
            }
        }
        return super.click(index, clickType, action);
    }

    private void tryToPurchase(ServerPlayer player, ItemStack soldItem, int slot, int targetInvSlot) {
        ItemStack cost = BwShopUtil.getCost(soldItem);
        if (cost == null) return;

        boolean isArmorItem = soldItem.getItem() instanceof ArmorItem;
        boolean isSword = soldItem.getItem() instanceof SwordItem;
        boolean isUpgradeable = soldItem.getOrCreateTag().contains(BwShopUpgradeables.upgradeableInfoKey);

        if (isArmorItem) {
            int playerLevel = ItemStackUtil.getArmorTier(player.inventory.getItem(36).getItem());
            int soldItemLevel = ItemStackUtil.getArmorTier(soldItem.getItem());
            if (soldItemLevel < playerLevel) {
                sendFail(player, "You already have better armor.");
                return;
            }
            if (soldItemLevel == playerLevel) {
                sendFail(player, "You already have this armor.");
                return;
            }
        }
        if (isUpgradeable && BwShopUpgradeables.hasSameUpgradeItem(player, soldItem)) {
            sendFail(player, "You have reached the maximum level of this item");
            return;
        }
        if (player.inventory.countItem(cost.getItem()) < cost.getCount()) {
            sendFail(player, "You can't afford this item");
            return;
        }
        soldItem = BwShopUtil.setBlockColor(player, soldItem);
        BwShopUtil.removeShopNbt(soldItem);
        if (!ItemStackUtil.hasRoomFor(player.inventory, soldItem) && !isArmorItem) {
            sendFail(player, "Your inventory is full.");
            return;
        }
        purchase(player, soldItem, cost, slot, targetInvSlot, isUpgradeable, isArmorItem, isSword);

    }

    private void purchase(ServerPlayer player, ItemStack soldItem, ItemStack cost, int slot, int targetInvSlot,
                          boolean isUpgradeable, boolean isArmorItem, boolean isSword) {

        PlayerUtil.removeItem(player, cost.getItem(), cost.getCount());
        playPurchaseSound(player, false);

        if (isUpgradeable) {
            BwShopUpgradeables.replaceUpgradeItem(player, soldItem, targetInvSlot);
            ItemStack shopSlotItem = BwShopUpgradeables.getUpgradeItem(player, guiSlotToIndex(slot));
            this.setSlot(slot, BwShopUtil.toGuiItem(shopSlotItem));

        } else if (isArmorItem) {
            BwShopUtil.giveArmorItems(player, soldItem);

        } else if (isSword) {
            BwShopUtil.giveSword(player, soldItem);

        } else {
            BwShopUtil.giveItem(player, soldItem, targetInvSlot);
        }
    }

    public static void sendFail(ServerPlayer player, String text) {
        PlayerUtil.getNexusPlayer(player).sendMessage(Component.text(text).color(ChatFormat.failColor));
        playPurchaseSound(player, true);
    }

    static int indexToGuiSlot(int index) {
        return 9 * (index % shopHeight + 1) + index / shopHeight + 1;
    }

    static int guiSlotToIndex(int guiSlot) {
        return shopHeight * (guiSlot % 9 - 1) + guiSlot / 9 - 1;
    }

    public static void playPurchaseSound(ServerPlayer player, boolean fail) {
        SoundEvent soundEvent;
        float volume;
        float pitch;
        if (!fail) {
            soundEvent = SoundEvents.NOTE_BLOCK_PLING;
            volume = 0.6f;
            pitch = 2.0f;
        } else {
            soundEvent = SoundEvents.ENDERMAN_TELEPORT;
            volume = 0.6f;
            pitch = 1.0f;
        }
        player.connection.send(new ClientboundSoundPacket(soundEvent, SoundSource.MASTER,
                player.getX(), player.getY(), player.getZ(), volume * 16, pitch));
    }

}
