package com.nexia.core.gui;

import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.ranks.NexiaRank;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

        for(NexiaRank rank : NexiaRank.ranks){

            if(slot == 17) {
                slot = 19;
            }

            boolean hasPermission = Permissions.check(player, rank.groupID);

            if(hasPermission && player.getTags().contains(rank.id)){
                ItemStack enchantedItem = new ItemStack(Items.NAME_TAG, 1);
                ItemDisplayUtil.addGlint(enchantedItem);
                enchantedItem.setHoverName(ObjectMappings.convertComponent(Component.text(rank.name, ChatFormat.brandColor2).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true)));
                this.setSlot(slot, enchantedItem);
                slot++;
            } else if(hasPermission){
                ItemStack changedItem = new ItemStack(Items.NAME_TAG, 1);
                changedItem.setHoverName(ObjectMappings.convertComponent(Component.text(rank.name, ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true)));
                this.setSlot(slot, changedItem);
                slot++;
            } else if(player.getTags().contains(rank.id)) {
                ItemStack enchantedItem = new ItemStack(Items.NAME_TAG, 1);
                ItemDisplayUtil.addGlint(enchantedItem);
                enchantedItem.setHoverName(ObjectMappings.convertComponent(Component.text(rank.name, ChatFormat.brandColor2).decoration(ChatFormat.italic, false).decoration(ChatFormat.bold, true)));
                this.setSlot(slot, enchantedItem);
                slot++;
            }
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            String name = itemStack.getHoverName().getString();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){

                NexiaPlayer player = new NexiaPlayer(this.player);

                player.sendNexiaMessage(
                        net.kyori.adventure.text.Component.text("Your prefix has been set to: ", ChatFormat.normalColor)
                                .append(net.kyori.adventure.text.Component.text(name, ChatFormat.brandColor2))
                                .append(net.kyori.adventure.text.Component.text(".", ChatFormat.normalColor))
                );

                for(NexiaRank rank : NexiaRank.ranks){
                    player.removeTag(rank.id);
                }

                if(name.equalsIgnoreCase("player")) name = "default";
                if(name.equalsIgnoreCase("nexia")) name = "supporter";

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
