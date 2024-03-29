package com.nexia.core.listeners.metis;

import com.combatreforged.metis.api.event.player.PlayerUseItemEvent;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.combatreforged.metis.api.world.item.ItemStack;
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

            if(sName.contains("gamemode selector") && gameMode == PlayerGameMode.LOBBY){
                //PlayGUI.openMainGUI(minecraftPlayer);
                player.runCommand("/play", 0, false);
            }

            if(sName.contains("prefix selector") && gameMode == PlayerGameMode.LOBBY){
                //PrefixGUI.openRankGUI(minecraftPlayer);
                player.runCommand("/prefix", 0, false);
            }

            if(sName.contains("queue sword") && gameMode == PlayerGameMode.LOBBY) {
                //QueueGUI.openQueueGUI(minecraftPlayer);
                player.runCommand("/queue", 0, false);
            }

            if(sName.contains("team axe") && gameMode == PlayerGameMode.LOBBY) {
                player.runCommand("/party list");
            }
        });
    }
}
