package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerUseItemEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.item.ItemStack;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;

public class UseItemListener {
    public static void registerListener() {
        PlayerUseItemEvent.BACKEND.register(playerUseItemEvent -> {

            Player player = playerUseItemEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            ItemStack itemStack = playerUseItemEvent.getItemStack();

            Component name = itemStack.getDisplayName();

            PlayerGameMode gameMode = PlayerDataManager.get(minecraftPlayer).gameMode;

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
