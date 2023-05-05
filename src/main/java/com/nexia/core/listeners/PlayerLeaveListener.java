package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerDisconnectEvent;
import com.combatreforged.factory.api.event.player.PlayerJoinEvent;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class PlayerLeaveListener {
    public static void registerListener() {
        PlayerDisconnectEvent.BACKEND.register(playerDisconnectEvent -> {

            Player player = playerDisconnectEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            playerDisconnectEvent.setLeaveMessage(ChatFormat.returnAppendedComponent(
                    Component.text("[").color(ChatFormat.lineColor),
                    Component.text("-").color(TextColor.fromHexString("#ff5340")),
                    Component.text("] ").color(ChatFormat.lineColor),
                    Component.text(player.getRawName()).color(ChatFormat.normalColor))
            );

            processDisconnect(player, minecraftPlayer);
        });
    }



    private static void runCommands(Player player){
        if(!ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerLeaveCommands, null)) {
            for (String command : Main.config.events.playerLeaveCommands) {
                ServerTime.factoryServer.runCommand(command);
            }
        }
        if(!ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverLeaveCommands, null)){
            for(String command : Main.config.events.serverLeaveCommands){
                player.runCommand(command);
            }
        }
    }

    private static void processDisconnect(Player player, ServerPlayer minecraftPlayer){
        PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.ffa.utilities.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.removePlayerData(minecraftPlayer);
        LobbyUtil.leaveAllGames(minecraftPlayer, true);
        runCommands(player);
    }
}
