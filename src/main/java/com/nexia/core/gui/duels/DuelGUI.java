package com.nexia.core.gui.duels;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DuelGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Duel Menu");

    ServerPlayer other;

    String kit;
    public DuelGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
        this.other = null;
        this.kit = "";
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 45; i++){
            this.setSlot(i, itemStack);
        }
    }

    private void setMapLayout(DuelGameMode gameMode){
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
            if(gameMode.gameMode == Minecraft.GameMode.ADVENTURE && !map.isAdventureSupported) continue;
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

        //this.setSlot(4, HeadFunctions.getPlayerHead(otherp.getScoreboardName(), 1));

        ItemStack playerHead = PlayerUtil.getPlayerHead(otherp.getUUID());
        playerHead.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text(otherp.getScoreboardName(), ChatFormat.Minecraft.yellow).decoration(ChatFormat.bold, true).decoration(ChatFormat.italic, false)));

        this.setSlot(4, playerHead);

        int i1 = 0;
        ItemStack item;
        for(String duel : DuelGameMode.stringDuelGameModes){
            if(slot == 17) {
                slot = 19;
            }
            if(slot == 26) {
                slot = 28;
            }
            item = DuelGameMode.duelsItems.get(i1);
            ItemDisplayUtil.removeLore(item, 0);
            ItemDisplayUtil.removeLore(item, 1);

            this.setSlot(slot, item);
            slot++;
            i1++;
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR && itemStack.getItem() != Items.PLAYER_HEAD){
                if(DuelGameMode.stringDuelGameModes.contains(name.getString().replaceAll(" ", "_"))){
                    this.kit = name.getString().replaceAll(" ", "_");
                    setMapLayout(GamemodeHandler.identifyGamemode(this.kit));
                } else {
                    GamemodeHandler.challengePlayer(new NexiaPlayer(this.player), new NexiaPlayer(this.other), this.kit, DuelsMap.identifyMap(name.getString()));
                    this.close();
                }

            }
        }
        return super.click(index, clickType, action);
    }
    public static int openDuelGui(ServerPlayer player, ServerPlayer other) {
        DuelGUI shop = new DuelGUI(MenuType.GENERIC_9x5, player, false);
        shop.setTitle(title);
        shop.setMainLayout(other);
        shop.open();
        return 1;
    }
}