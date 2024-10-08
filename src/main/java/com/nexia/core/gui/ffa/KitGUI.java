package com.nexia.core.gui.ffa;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.ffa.kits.FfaKit;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.loader.impl.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class KitGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Kits Menu");

    public KitGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 36; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);

        int slot = 10;
        int airSlots = 10;
        for(int air = 0; air < 14; air++){
            if(airSlots == 17) {
                airSlots = 19;
            }
            this.setSlot(airSlots, new ItemStack(Items.AIR));
            airSlots++;
        }
        for(FfaKit ffaKits : FfaKit.ffaKits){
            if(slot == 17) {
                slot = 19;
            }

            ItemStack item = ffaKits.item;

            item.setHoverName(ObjectMappings.convertComponent(Component.text(StringUtil.capitalize(ffaKits.id.replaceAll("_", " ")), ChatFormat.Minecraft.white)));

            this.setSlot(slot, item);

            slot++;
        }
    }

    public static void giveKit(ServerPlayer minecraftPlayer, String name) {
        NexiaPlayer player = new NexiaPlayer(minecraftPlayer);
        FfaKit kit = FfaKit.identifyKit(name);
        if(kit != null) kit.giveKit(new NexiaPlayer(minecraftPlayer), false);
        else player.sendMessage(Component.text("Invalid kit!", ChatFormat.failColor));
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            net.minecraft.network.chat.Component name = itemStack.getHoverName();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
                String modifiedName = name.getString().toLowerCase();
                giveKit(this.player, modifiedName);
                this.close();
            }

        }
        return super.click(index, clickType, action);
    }
    public static void openKitGUI(ServerPlayer player) {
        KitGUI shop = new KitGUI(MenuType.GENERIC_9x4, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
    }
}