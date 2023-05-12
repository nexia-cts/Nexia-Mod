package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerUseItemEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.item.ItemStack;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.PlayGUI;
import com.nexia.core.gui.PrefixGUI;
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
                PlayGUI.openMainGUI(minecraftPlayer);
            }

            if(sName.contains("prefix selector") && gameMode == PlayerGameMode.LOBBY){
                PrefixGUI.openRankGUI(minecraftPlayer);
            }

        });
    }
}
