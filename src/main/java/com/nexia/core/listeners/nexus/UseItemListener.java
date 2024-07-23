package com.nexia.core.listeners.nexus;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.Main;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.nexus.api.event.player.PlayerUseItemEvent;
import com.nexia.nexus.api.world.item.ItemStack;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.NexiaPlayer;
import net.kyori.adventure.text.Component;

public class UseItemListener {
    public void registerListener() {
        PlayerUseItemEvent.BACKEND.register(playerUseItemEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerUseItemEvent.getPlayer());

            ItemStack itemStack = playerUseItemEvent.getItemStack();

            Component name = itemStack.getDisplayName();

            PlayerGameMode gameMode = ((CorePlayerData)PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(player)).gameMode;
            String sName = name.toString().toLowerCase();

            if(gameMode == PlayerGameMode.LOBBY) {
                if(sName.contains("gamemode selector")){
                    //PlayGUI.openMainGUI(minecraftPlayer);
                    player.runCommand("/play", 0, false);
                    return;
                }

                if(sName.contains("prefix selector")){
                    //PrefixGUI.openRankGUI(minecraftPlayer);
                    player.runCommand("/prefix", 0, false);
                    return;
                }

                if(sName.contains("duel sword") && !sName.contains("custom duel sword")) {
                    //QueueGUI.openQueueGUI(minecraftPlayer);
                    player.runCommand("/queue", 0, false);
                    return;
                }

                if(sName.contains("team axe")) {
                    player.runCommand("/party list");
                }
            }
        });
    }
}
