package com.nexia.core.gui;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class PrefixGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Prefix Menu");
    public PrefixGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack, int slots){
        for(int i = 0; i < slots; i++){
            this.setSlot(i, itemStack);
            /*
            GuiElementInterface element = this.getSlot(i);
            if(element != null && element.getItemStack().getItem() == null || element.getItemStack().getItem() == Items.AIR){
                this.setSlot(i, itemStack);
            }
             */
        }
    }
    private void setMainLayout(ServerPlayer player){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot, 36);
        int slot = 10;
        int airSlots = 10;
        for(int air = 0; air < 14; air++){
            if(airSlots == 17) {
                airSlots = 19;
            }
            this.setSlot(airSlots, new ItemStack(Items.AIR));
            airSlots++;
        }
        for(int i = 0; i < 9; i++){
            if(slot == 17) {
                slot = 19;
            }

            if(PlayerUtil.hasPermission(player.createCommandSourceStack(), "nexia.prefix." + Main.config.ranks[i], 4) && player.getTags().contains(Main.config.ranks[i])){
                ItemStack enchantedItem = new ItemStack(Items.NAME_TAG, 1);
                enchantedItem.enchant(Enchantments.SHARPNESS, 1);
                enchantedItem.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
                enchantedItem.setHoverName(new TextComponent("§d§l" + Main.config.ranks[i]));
                this.setSlot(slot, enchantedItem);
                slot++;
            } else if(PlayerUtil.hasPermission(player.createCommandSourceStack(), "nexia.prefix." + Main.config.ranks[i], 4)){
                ItemStack changedItem = new ItemStack(Items.NAME_TAG, 1);
                changedItem.setHoverName(new TextComponent("§f" + Main.config.ranks[i]));
                this.setSlot(slot, changedItem);
                slot++;
            } else if(player.getTags().contains(Main.config.ranks[i])) {
                ItemStack enchantedItem = new ItemStack(Items.NAME_TAG, 1);
                enchantedItem.enchant(Enchantments.SHARPNESS, 1);
                enchantedItem.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
                enchantedItem.setHoverName(new TextComponent(Main.config.ranks[i]).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
                this.setSlot(slot, enchantedItem);
                slot++;
            }
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            String name = itemStack.getHoverName().getString().substring(2).replaceAll("§l", "");

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){

                Player player = PlayerUtil.getFactoryPlayer(this.player);

                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(net.kyori.adventure.text.Component.text("Your prefix has been set to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(net.kyori.adventure.text.Component.text(name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                .append(net.kyori.adventure.text.Component.text(".").decoration(ChatFormat.bold, false))
                );

                for(int i = 0; i < 9; i++){
                    player.removeTag(Main.config.ranks[i]);
                }

                player.addTag(name.toLowerCase());
                this.setMainLayout(this.player);
            }

        }
        return super.click(index, clickType, action);
    }
    public static void openRankGUI(ServerPlayer player) {
        PrefixGUI shop = new PrefixGUI(MenuType.GENERIC_9x4, player, false);
        shop.setTitle(title);
        shop.setMainLayout(player);
        shop.open();
    }
}