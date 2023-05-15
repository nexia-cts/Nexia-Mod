package com.nexia.core.gui;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class PlayGUI extends SimpleGui {

    static final TextComponent title = new TextComponent("Game Menu");
    static final TextComponent ffaTitle = new TextComponent("FFA Menu");

    static final TextComponent minigamesTitle = new TextComponent("Minigames Menu");
    public PlayGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
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
    private void setMainLayout(){
        ItemStack enchanted_sword = new ItemStack(Items.NETHERITE_SWORD, 1);
        enchanted_sword.setHoverName(new TextComponent("§7§lFFA"));
        enchanted_sword.enchant(Enchantments.SHARPNESS, 1);
        enchanted_sword.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        enchanted_sword.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack enchanted_barrier = new ItemStack(Items.BARRIER, 1);
        enchanted_barrier.setHoverName(new TextComponent("§c§lHub"));
        enchanted_barrier.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack enchanted_compass = new ItemStack(Items.COMPASS, 1);
        enchanted_compass.setHoverName(new TextComponent("§f§lMinigames"));
        enchanted_compass.enchant(Enchantments.SHARPNESS, 1);
        enchanted_compass.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        enchanted_compass.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot, 27);
        this.setSlot(11, enchanted_sword);
        this.setSlot(13, enchanted_barrier);
        this.setSlot(15, enchanted_compass);
    }

    private void setFFALayout(){
        this.setTitle(ffaTitle);
        ItemStack enchanted_sword = new ItemStack(Items.NETHERITE_SWORD, 1);
        enchanted_sword.setHoverName(new TextComponent("§7§lClassic"));
        enchanted_sword.enchant(Enchantments.SHARPNESS, 1);
        enchanted_sword.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        enchanted_sword.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack unknown = new ItemStack(Items.BARRIER, 1);
        unknown.setHoverName(new TextComponent("§7§l???"));
        unknown.enchant(Enchantments.SHARPNESS, 1);
        unknown.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        unknown.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot, 27);
        this.setSlot(11, unknown);
        this.setSlot(13, enchanted_sword);
        this.setSlot(15, unknown);
    }

    private void setMinigamesLayout(){
        this.setTitle(minigamesTitle);
        ItemStack unknown = new ItemStack(Items.BARRIER, 1);
        unknown.setHoverName(new TextComponent("§7§l???"));
        unknown.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack duels = new ItemStack(Items.DIAMOND_SWORD, 1);
        duels.setHoverName(new TextComponent("§b§lDuels"));
        duels.enchant(Enchantments.SHARPNESS, 1);
        duels.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        duels.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot, 27);
        this.setSlot(11, duels);
        this.setSlot(13, unknown);
        this.setSlot(15, unknown);
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();
            if(name.getString().equalsIgnoreCase("§7§lClassic")){
                LobbyUtil.sendGame(this.player, "classic ffa", true, true);
                this.close();
            }
            if(name.getString().equalsIgnoreCase("§7§lFFA")){
                this.setFFALayout();
            }
            if(name.getString().equalsIgnoreCase("§f§lMinigames")){
                this.setMinigamesLayout();
            }
            if(name.getString().equalsIgnoreCase("§b§lDuels")){
                this.close();
            }


            if(name.getString().toLowerCase().contains("hub")){
                LobbyUtil.leaveAllGames(this.player, true);
            }

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR && itemStack.getItem() != Items.NETHERITE_SWORD && itemStack.getItem() != Items.COMPASS){
                this.close();
            }
        }
        return super.click(index, clickType, action);
    }
    public static void openMainGUI(ServerPlayer player) {
        PlayGUI shop = new PlayGUI(MenuType.GENERIC_9x3, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
    }
}
