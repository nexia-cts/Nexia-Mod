package com.nexia.core.gui;

import com.combatreforged.factory.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.discord.Main;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RanksGUI extends SimpleGui {

    static final TextComponent title = new TextComponent("Ranks Menu");
    public RanksGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 27; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){
        ItemStack supporter = new ItemStack(Items.DRAGON_BREATH, 1);
        supporter.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Supporter", ChatFormat.brandColor2).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true)));
        ItemDisplayUtil.addGlint(supporter);
        supporter.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text("How to get it:", ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false), 0);
        ItemDisplayUtil.addLore(supporter, "§1", 1);
        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text("Patreon - Buy it from our patreon.", TextColor.fromHexString("#f96b59")).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text("https://www.patreon.com/Nexia", TextColor.fromHexString("#f96b59")).decoration(ChatFormat.italic, false), 3);
        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text("OR", ChatFormat.Minecraft.white).decoration(ChatFormat.bold, true).decoration(ChatFormat.italic, false), 4);
        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text("Boosting - Boost the discord server.", TextColor.fromHexString("#8c00ff")).decoration(ChatFormat.italic, false), 5);
        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text(Main.config.discordLink, TextColor.fromHexString("#8c00ff")).decoration(ChatFormat.italic, false), 6);
        ItemDisplayUtil.addLore(supporter, "§3", 7);
        ItemDisplayUtil.addLore(supporter, net.kyori.adventure.text.Component.text("Price: ", TextColor.fromHexString("#8c00ff")).decoration(ChatFormat.italic, false)
                        .append(net.kyori.adventure.text.Component.text("5.00$", ChatFormat.brandColor1).decoration(ChatFormat.bold, true).decoration(ChatFormat.italic, false)), 8);

        ItemStack purple = new ItemStack(Items.PURPLE_STAINED_GLASS_PANE, 1);
        purple.setHoverName(new TextComponent(""));

        ItemStack magenta = new ItemStack(Items.MAGENTA_STAINED_GLASS_PANE, 1);
        magenta.setHoverName(new TextComponent(""));

        ItemStack magenta_glow = new ItemStack(Items.MAGENTA_STAINED_GLASS_PANE, 1);
        ItemDisplayUtil.addGlint(magenta_glow);
        magenta_glow.setHoverName(new TextComponent(""));

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);

        this.setSlot(2, purple);
        this.setSlot(3, magenta_glow);
        this.setSlot(4, magenta_glow);
        this.setSlot(5, magenta_glow);
        this.setSlot(6, purple);
        this.setSlot(11, magenta);
        this.setSlot(12, magenta_glow);
        this.setSlot(13, supporter);
        this.setSlot(14, magenta_glow);
        this.setSlot(15, magenta);
        this.setSlot(20, purple);
        this.setSlot(21, magenta_glow);
        this.setSlot(22, magenta_glow);
        this.setSlot(23, magenta_glow);
        this.setSlot(24, purple);
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

            if(name.getString().contains("Supporter")){
                nexiaPlayer.sendMessage(ChatFormat.nexiaMessage.append(
                                net.kyori.adventure.text.Component.text("In order to get the ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                        .append(net.kyori.adventure.text.Component.text("Supporter").color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                        .append(net.kyori.adventure.text.Component.text(" rank, you need to either ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                        .append(net.kyori.adventure.text.Component.text("buy from our patreon")
                                                .color(TextColor.fromHexString("#f96b59"))
                                                .decoration(ChatFormat.bold, true)
                                                .hoverEvent(HoverEvent.showText(net.kyori.adventure.text.Component.text("Click me").color(TextColor.fromHexString("#f96b59"))))
                                                .clickEvent(ClickEvent.openUrl("https://www.patreon.com/Nexia")))
                                        .append(net.kyori.adventure.text.Component.text(" or ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                        .append(net.kyori.adventure.text.Component.text("boost our discord server")
                                                .color(TextColor.fromHexString("#8c00ff"))
                                                .decoration(ChatFormat.bold, true)
                                                .hoverEvent(HoverEvent.showText(net.kyori.adventure.text.Component.text("Click me").color(TextColor.fromHexString("#8c00ff"))))
                                                .clickEvent(ClickEvent.openUrl(Main.config.discordLink)))
                                        .append(net.kyori.adventure.text.Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                        )
                );
            }

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.MAGENTA_STAINED_GLASS_PANE && itemStack.getItem() != Items.PURPLE_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
                this.close();
            }
        }
        return super.click(index, clickType, action);
    }
    public static void openMainGUI(ServerPlayer player) {
        RanksGUI shop = new RanksGUI(MenuType.GENERIC_9x3, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
    }
}