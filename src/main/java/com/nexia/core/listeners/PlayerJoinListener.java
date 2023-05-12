package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerJoinEvent;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

public class PlayerJoinListener {
    public static void registerListener() {
        PlayerJoinEvent.BACKEND.register(playerJoinEvent -> {

            Player player = playerJoinEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            processJoin(player, minecraftPlayer);

            /*
            if(!Main.config.events.statusMessages) { return; }


            if(minecraftPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) <= 1) {
                playerJoinEvent.setJoinMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("!").color(TextColor.fromHexString("#ff9940")))
                                                        .append(Component.text("] ").color(ChatFormat.lineColor))
                                                                .append(Component.text(player.getRawName()).color(TextColor.fromHexString("#ff9940")))


                );
            } else {
                playerJoinEvent.setJoinMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                        .append(Component.text("+").color(ChatFormat.greenColor))
                                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                                        .append(Component.text(player.getRawName()).color(ChatFormat.greenColor))
                );
            }

             */
        });
    }



    private static void runCommands(Player player, ServerPlayer minecraftPlayer){
        if(minecraftPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) <= 1) {
            if(!ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerFirstJoinCommands, null)) {
                for (String command : Main.config.events.playerFirstJoinCommands) {
                    ServerTime.factoryServer.runCommand(command);
                }
            }
            if(!ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerFirstJoinCommands, null)){
                for(String command : Main.config.events.serverFirstJoinCommands){
                    player.runCommand(command);
                }
            }
        }

        if(!ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerJoinCommands, null)) {
            for (String command : Main.config.events.playerJoinCommands) {
                ServerTime.factoryServer.runCommand(command);
            }
        }
        if(!ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverJoinCommands, null)){
            for(String command : Main.config.events.serverJoinCommands){
                player.runCommand(command);
            }
        }
    }

    private static void sendJoinMessage(Player player){
        player.sendMessage(ChatFormat.separatorLine("Welcome"));
        player.sendMessage(
                Component.text(" » ").color(ChatFormat.brandColor2)
                                .append(Component.text("Welcome ").color(ChatFormat.normalColor))
                                        .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2))
                                                .append(Component.text(" to ").color(ChatFormat.normalColor))
                                                        .append(Component.text("Nexia").color(ChatFormat.brandColor2))
                                                                .append(Component.text("!").color(ChatFormat.normalColor))
        );
        player.sendMessage(
                Component.text(" » ").color(ChatFormat.brandColor2)
                                .append(Component.text("Players online: ").color(ChatFormat.normalColor))
                                        .append(Component.text(ServerTime.minecraftServer.getPlayerCount()).color(ChatFormat.brandColor2))
                                                .append(Component.text("/").color(ChatFormat.lineColor))
                                                        .append(Component.text(ServerTime.factoryServer.getMaxPlayerCount()).color(ChatFormat.brandColor2))
        );
        player.sendMessage(
                Component.text(" » ").color(ChatFormat.brandColor2)
                                .append(Component.text("Read the rules: ").color(ChatFormat.normalColor))
                                        .append(Component.text("/rules")).color(ChatFormat.brandColor2).hoverEvent(HoverEvent.showText(Component.text("Click me").color(TextColor.fromHexString("#73ff54"))))
                        .clickEvent(ClickEvent.suggestCommand("/rules"))
        );
        player.sendMessage(
                Component.text(" » ").color(ChatFormat.brandColor2)
                                .append(Component.text("Join our discord: ").color(ChatFormat.normalColor))
                                        .append(Component.text(Main.config.discordLink)
                                                .color(ChatFormat.brandColor2)
                                                .hoverEvent(HoverEvent.showText(Component.text("Click me").color(TextColor.fromHexString("#73ff54"))))
                                                .clickEvent(ClickEvent.openUrl(Main.config.discordLink))
                                        )
        );
        player.sendMessage(ChatFormat.separatorLine(null));
    }

    private static void processJoin(Player player, ServerPlayer minecraftPlayer){
        PlayerDataManager.addPlayerData(minecraftPlayer);
        com.nexia.ffa.utilities.player.PlayerDataManager.addPlayerData(minecraftPlayer);
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.addPlayerData(minecraftPlayer);
        LobbyUtil.leaveAllGames(minecraftPlayer, true);
        runCommands(player, minecraftPlayer);
        sendJoinMessage(player);
    }
}
