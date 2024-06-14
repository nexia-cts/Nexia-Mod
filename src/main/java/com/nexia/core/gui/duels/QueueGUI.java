package com.nexia.core.gui.duels;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
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


            item = DuelGameMode.duelsItems.get(i1).setHoverName(new TextComponent("§f" + duel.toUpperCase().replaceAll("_", " ")));
            DuelGameMode gameMode = GamemodeHandler.identifyGamemode(duel);

            ItemDisplayUtil.removeLore(item, 0);
            assert gameMode != null;
            ItemDisplayUtil.addLore(item, "§7There are §7§l" + gameMode.queue.size() + " §7people queued up.", 0);

            if(GamemodeHandler.isInQueue(new NexiaPlayer(this.player), gameMode)) {
                ItemDisplayUtil.removeLore(item, 1);
                ItemDisplayUtil.addLore(item, "§7Click to leave the queue.", 1);
            } else {
                ItemDisplayUtil.removeLore(item, 1);
            }

            this.setSlot(slot, item);
            slot++;
            i1++;
        }
        this.setSlot(40, new ItemStack(Items.BARRIER).setHoverName(new TextComponent("§c§lLeave ALL Queues")));
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
                if(name.getString().substring(4).equalsIgnoreCase("Leave ALL Queues")) {
                    //PlayerUtil.getFactoryPlayer(player).runCommand("/queue LEAVE", 0, false);
                    LobbyUtil.leaveAllGames(this.player, true);
                    this.close();
                    return super.click(index, clickType, action);
                }

                String modifiedName = name.getString().substring(2).replaceAll(" ", "_");
                DuelGameMode gameMode = GamemodeHandler.identifyGamemode(modifiedName);

                if(gameMode != null && GamemodeHandler.isInQueue(player, gameMode)) {
                    GamemodeHandler.removeQueue(this.player, modifiedName, false);
                    this.close();
                } else {
                    GamemodeHandler.joinQueue(this.player, modifiedName, false);
                    this.close();
                }
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