package com.nexia.core.gui.duels;

import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class CustomDuelGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Custom Duel Menu");

    ServerPlayer other;

    String kit;
    public CustomDuelGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
        this.other = null;
        this.kit = "";
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 45; i++){
            this.setSlot(i, itemStack);
        }
    }

    private void setMapLayout(){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        int slot = 10;
        int airSlots = 10;
        for(int air = 0; air < 21; air++){
            if(airSlots == 17) {
                airSlots = 19;
            }
            if(airSlots == 26) {
                airSlots = 28;
            }
            this.setSlot(airSlots, new ItemStack(Items.AIR));
            airSlots++;
        }
        for(DuelsMap map : DuelsMap.duelsMaps){
            this.setSlot(slot, map.item);
            slot++;
        }
    }

    private void setMainLayout(ServerPlayer otherp){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        this.other = otherp;

        fillEmptySlots(emptySlot);
        int slot = 10;
        int airSlots = 10;
        for(int air = 0; air < 21; air++){
            if(airSlots == 17) {
                airSlots = 19;
            }
            if(airSlots == 26) {
                airSlots = 28;
            }
            this.setSlot(airSlots, new ItemStack(Items.AIR));
            airSlots++;
        }


        ItemStack playerHead = PlayerUtil.getPlayerHead(otherp.getUUID());
        playerHead.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text(otherp.getScoreboardName(), ChatFormat.Minecraft.yellow).decoration(ChatFormat.bold, true).decoration(ChatFormat.italic, false)));

        this.setSlot(4, playerHead);

        ArrayList<String> inventories = InventoryUtil.getListOfInventories("duels/custom/" + this.player.getStringUUID());

        ItemStack item;

        if(inventories.isEmpty()) {
            item = new ItemStack(Items.COMMAND_BLOCK);

            ItemDisplayUtil.removeLore(item, 0);
            ItemDisplayUtil.removeLore(item, 1);

            ItemDisplayUtil.addLore(item,
                    net.kyori.adventure.text.Component.text("Use ", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false)
                            .append(net.kyori.adventure.text.Component.text("/kiteditor edit <1, 2, 3, smp, vanilla>", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true))
                            .append(net.kyori.adventure.text.Component.text(" to edit/create a custom kit!", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false))
                    , 0);

            ItemDisplayUtil.addGlint(item);

            item.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("No kits!", ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false)));

            this.setSlot(slot, item);
            return;
        }

        for(String inventory : inventories){
            if(slot == 17) {
                slot = 19;
            }
            if(slot == 26) {
                slot = 28;
            }


            item = new ItemStack(Items.BARRIER);

            ItemDisplayUtil.removeLore(item, 0);
            ItemDisplayUtil.removeLore(item, 1);

            net.kyori.adventure.text.Component infoMsg = net.kyori.adventure.text.Component.text("INFO: ", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true)
                    .append(net.kyori.adventure.text.Component.text("This kit is a ", ChatFormat.Minecraft.yellow)).decoration(ChatFormat.italic, false)
                    .append(net.kyori.adventure.text.Component.text("per-custom ", ChatFormat.Minecraft.yellow)).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true)
                    .append(net.kyori.adventure.text.Component.text("kit.", ChatFormat.Minecraft.yellow)).decoration(ChatFormat.italic, false);

            if(inventory.equalsIgnoreCase("vanilla")) {
                item = new ItemStack(Items.RESPAWN_ANCHOR);

                ItemDisplayUtil.addLore(item, infoMsg, 0);
            }
            else if(inventory.equalsIgnoreCase("smp")) {
                item = new ItemStack(Items.DIAMOND_CHESTPLATE);

                ItemDisplayUtil.addLore(item, infoMsg, 0);
            }

            ItemDisplayUtil.addGlint(item);

            item.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text(inventory.toUpperCase().replaceAll("_", " "), ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false)));

            this.setSlot(slot, item);
            slot++;
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR && itemStack.getItem() != Items.PLAYER_HEAD && itemStack.getItem() != Items.COMMAND_BLOCK){
                if(InventoryUtil.getListOfInventories("duels/custom/" + this.player.getStringUUID()).contains(name.getString().toLowerCase().replaceAll(" ", "_"))){
                    this.kit = name.getString().toLowerCase().replaceAll(" ", "_");
                    setMapLayout();
                } else {
                    GamemodeHandler.customChallengePlayer(new NexiaPlayer(this.player), new NexiaPlayer(this.other), this.kit, DuelsMap.identifyMap(name.getString()));
                    this.close();
                }

            }
        }
        return super.click(index, clickType, action);
    }
    public static int openDuelGui(ServerPlayer player, ServerPlayer other) {
        CustomDuelGUI shop = new CustomDuelGUI(MenuType.GENERIC_9x5, player, false);
        shop.setTitle(title);
        shop.setMainLayout(other);
        shop.open();
        return 1;
    }
}