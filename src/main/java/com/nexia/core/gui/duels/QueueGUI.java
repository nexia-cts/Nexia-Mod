package com.nexia.core.gui.duels;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
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

public class QueueGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Queue Menu");
    public QueueGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 45; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){
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
            DuelGameMode gameMode = GamemodeHandler.identifyGamemode(duel);

            ItemDisplayUtil.removeLore(item, 0);
            assert gameMode != null;
            ItemDisplayUtil.addLore(item,
                    net.kyori.adventure.text.Component.text("There are ", ChatFormat.Minecraft.gray)
                            .append(net.kyori.adventure.text.Component.text(gameMode.queue.size(), ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                            .append(net.kyori.adventure.text.Component.text(" people queued up.", ChatFormat.Minecraft.gray))
            , 0);

            ItemDisplayUtil.removeLore(item, 1);
            if(GamemodeHandler.isInQueue(new NexiaPlayer(this.player), gameMode)) {
                ItemDisplayUtil.addLore(item, net.kyori.adventure.text.Component.text("Click to leave the queue.", ChatFormat.Minecraft.gray), 1);
            }

            this.setSlot(slot, item);
            slot++;
            i1++;
        }
        this.setSlot(40, new ItemStack(Items.BARRIER).setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Leave ALL Queues", ChatFormat.Minecraft.red).decoration(ChatFormat.bold, true).decoration(ChatFormat.italic, false))));
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
                if(name.getString().equalsIgnoreCase("Leave ALL Queues")) {
                    GamemodeHandler.removeQueue(nexiaPlayer, null, false);
                }

                String modifiedName = name.getString().replaceAll(" ", "_");
                DuelGameMode gameMode = GamemodeHandler.identifyGamemode(modifiedName);

                if(gameMode != null && GamemodeHandler.isInQueue(nexiaPlayer, gameMode)) {
                    GamemodeHandler.removeQueue(nexiaPlayer, modifiedName, false);
                } else {
                    GamemodeHandler.joinQueue(nexiaPlayer, modifiedName, false);
                }

                this.close();
            }

        }
        return super.click(index, clickType, action);
    }
    public static int openQueueGUI(ServerPlayer player) {
        QueueGUI shop = new QueueGUI(MenuType.GENERIC_9x5, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
        return 1;
    }
}