package com.nexia.core.gui;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
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
        ItemStack ffa = new ItemStack(Items.NETHERITE_SWORD, 1);
        ffa.setHoverName(new TextComponent("§3FFA"));
        ItemDisplayUtil.addGlint(ffa);
        ffa.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(ffa, "§5", 0);
        ItemDisplayUtil.addLore(ffa, "§7Fight players in a huge landscape", 1);
        ItemDisplayUtil.addLore(ffa, "§7be the best player.", 2);
        ItemDisplayUtil.addLore(ffa, "§f", 3);
        ItemDisplayUtil.addLore(ffa, "§3◆ There are " + PlayerGameMode.FFA.players + " people playing this gamemode.", 4);

        ItemStack hub = new ItemStack(Items.DRAGON_BREATH, 1);
        hub.setHoverName(new TextComponent("§5Hub"));
        hub.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        ItemDisplayUtil.addLore(hub, "§7Return back to the hub.", 0);

        ItemStack bedwars = new ItemStack(Items.RED_BED, 1);
        bedwars.setHoverName(new TextComponent("§cBedwars"));
        ItemDisplayUtil.addGlint(bedwars);
        bedwars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(bedwars, "§5", 0);
        ItemDisplayUtil.addLore(bedwars, "§7Protect your bed and", 1);
        ItemDisplayUtil.addLore(bedwars, "§7destroy other's beds, kill your", 2);
        ItemDisplayUtil.addLore(bedwars, "§7opponents to win!", 3);
        ItemDisplayUtil.addLore(bedwars, "§f", 4);
        ItemDisplayUtil.addLore(bedwars, "§c◆ There are " + PlayerGameMode.BEDWARS.players + " people playing this gamemode.", 5);

        ItemStack oitc = new ItemStack(Items.BOW, 1);
        oitc.setHoverName(new TextComponent("§eOITC"));
        ItemDisplayUtil.addGlint(oitc);
        oitc.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(oitc, "§5", 0);
        ItemDisplayUtil.addLore(oitc, "§7One in the chamber.", 1);
        ItemDisplayUtil.addLore(oitc, "§7Try to kill as many people as possible", 2);
        ItemDisplayUtil.addLore(oitc, "§7to achieve victory!", 3);
        ItemDisplayUtil.addLore(oitc, "§f", 4);
        ItemDisplayUtil.addLore(oitc, "§e◆ There are " + PlayerGameMode.OITC.players + " people playing this gamemode.", 5);

        ItemStack duels = new ItemStack(Items.NETHERITE_AXE, 1);
        duels.setHoverName(new TextComponent("§9Duels"));
        ItemDisplayUtil.addGlint(duels);
        duels.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        int duelsPlayers = 0;
        for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            DuelGameMode gameMode = PlayerDataManager.get(serverPlayer).gameMode;
            if(gameMode != null && (gameMode != DuelGameMode.LOBBY && gameMode != DuelGameMode.SPECTATING)) duelsPlayers++;
        }

        ItemDisplayUtil.addLore(duels, "§5", 0);
        ItemDisplayUtil.addLore(duels, "§7Duel against other people", 1);
        ItemDisplayUtil.addLore(duels, "§7or play against people in teams", 2);
        ItemDisplayUtil.addLore(duels, "§7with team duels!", 3);
        ItemDisplayUtil.addLore(duels, "§f", 4);
        ItemDisplayUtil.addLore(duels, "§9◆ There are " + (LobbyUtil.lobbyWorld.players().size() + duelsPlayers) + " people playing this gamemode.", 5);


        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot, 9);
        this.setSlot(2, ffa);
        this.setSlot(4, hub);
        this.setSlot(6, duels);
        this.setSlot(0, oitc);
        this.setSlot(8, bedwars);
    }

    private void setFFALayout(){
        this.setTitle(ffaTitle);
        ItemStack enchanted_sword = new ItemStack(Items.NETHERITE_SWORD, 1);
        enchanted_sword.setHoverName(new TextComponent("§cClassic"));
        enchanted_sword.enchant(Enchantments.SHARPNESS, 1);
        enchanted_sword.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        enchanted_sword.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack unknown = new ItemStack(Items.BARRIER, 1);
        unknown.setHoverName(new TextComponent("§c???"));
        ItemDisplayUtil.addGlint(unknown);
        unknown.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot, 9);
        this.setSlot(3, unknown);
        this.setSlot(4, enchanted_sword);
        this.setSlot(5, unknown);
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();
            if(name.getString().equalsIgnoreCase("§cClassic")){
                LobbyUtil.sendGame(this.player, "classic ffa", true, true);
                this.close();
            }
            if(name.getString().equalsIgnoreCase("§3FFA")){
                this.setFFALayout();
            }
            if(name.getString().equalsIgnoreCase("§cBedwars")){
                LobbyUtil.sendGame(this.player, "bedwars", true, true);
                this.close();
            }
            if(name.getString().equalsIgnoreCase("§eOITC")){
                LobbyUtil.sendGame(this.player, "oitc", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§9Duels")){
                LobbyUtil.sendGame(this.player, "duels", true, true);
                this.close();
            }

            if(name.getString().toLowerCase().equalsIgnoreCase("§5Hub")){
                LobbyUtil.leaveAllGames(this.player, true);
            }

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR && itemStack.getItem() != Items.NETHERITE_SWORD && itemStack.getItem() != Items.COMPASS){
                this.close();
            }
        }
        return super.click(index, clickType, action);
    }
    public static void openMainGUI(ServerPlayer player) {
        PlayGUI shop = new PlayGUI(MenuType.GENERIC_9x1, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
    }
}
