package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerData;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import com.nexia.nexus.api.event.player.PlayerJoinEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.stats.Stats;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.nexia.discord.NexiaDiscord.jda;

public class PlayerJoinListener {
    public void registerListener() {
        PlayerJoinEvent.BACKEND.register(playerJoinEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerJoinEvent.getPlayer());
            if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).clientType.equals(CorePlayerData.ClientType.VIAFABRICPLUS)) return;

            //setJoinMessage(player, playerJoinEvent);

            CompletableFuture.runAsync(() -> PlayerDataManager.dataManagerMap.forEach((resourceLocation, playerDataManager) -> playerDataManager.addPlayerData(player)));

            LobbyUtil.returnToLobby(player, true);
            checkBooster(player);

            ServerTime.scheduler.schedule(() -> {
                sendJoinMessage(player);
            }, 10);


        });
    }

    private static void setJoinMessage(NexiaPlayer player, PlayerJoinEvent playerJoinEvent) {
        if(player.unwrap().getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) {
            playerJoinEvent.setJoinMessage(
                    net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                            .append(net.kyori.adventure.text.Component.text("!").color(ChatFormat.goldColor)
                                    .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                                    .append(net.kyori.adventure.text.Component.text(player.getRawName()).color(ChatFormat.goldColor)))
            );
        } else {
            playerJoinEvent.setJoinMessage(
                    net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                            .append(net.kyori.adventure.text.Component.text("+").color(ChatFormat.greenColor))
                            .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                            .append(net.kyori.adventure.text.Component.text(player.getRawName()).color(ChatFormat.greenColor))
            );
        }
    }

    private static void sendJoinMessage(NexiaPlayer player){
        player.sendMessage(ChatFormat.separatorLine("Welcome"));
        player.sendMessage(
                Component.text(" » ", ChatFormat.brandColor2)
                                .append(Component.text("Welcome ", ChatFormat.normalColor))
                                        .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                                                .append(Component.text(" to ", ChatFormat.normalColor))
                                                        .append(Component.text("Nexia", ChatFormat.brandColor2))
                                                                .append(Component.text("!", ChatFormat.normalColor))
        );
        player.sendMessage(
                Component.text(" » ", ChatFormat.brandColor2)
                                .append(Component.text("Players online: ", ChatFormat.normalColor))
                                        .append(Component.text(ServerTime.minecraftServer.getPlayerCount(), ChatFormat.brandColor2))
                                                .append(Component.text("/").color(ChatFormat.lineColor))
                                                        .append(Component.text(ServerTime.nexusServer.getMaxPlayerCount(), ChatFormat.brandColor2))
        );
        player.sendMessage(
                Component.text(" » ", ChatFormat.brandColor2)
                                .append(Component.text("Read the rules: ", ChatFormat.normalColor))
                                        .append(Component.text("/rules", ChatFormat.brandColor2)
                                                .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.Minecraft.green))))
                                                .clickEvent(ClickEvent.suggestCommand("/rules"))
        );
        player.sendMessage(
                Component.text(" » ", ChatFormat.brandColor2)
                                .append(Component.text("Join our discord: ", ChatFormat.normalColor))
                                        .append(Component.text(NexiaDiscord.config.discordLink)
                                                .color(ChatFormat.brandColor2)
                                                .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.Minecraft.green)))
                                                .clickEvent(ClickEvent.openUrl(NexiaDiscord.config.discordLink))
                                        )
        );
        player.sendMessage(ChatFormat.separatorLine(null));
    }

    private static void checkBooster(NexiaPlayer player) {
        PlayerData playerData = PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).get(player.getUUID());
        if(!playerData.savedData.get(Boolean.class, "isLinked")) { return; }
        Member discordUser = null;

        try {
            discordUser = Objects.requireNonNull(jda.getGuildById(NexiaDiscord.config.guildID)).retrieveMemberById(playerData.savedData.get(Long.class, "discordID")).complete(true);
        } catch (Exception ignored) { }

        if(discordUser == null) {
            if(player.hasPrefix(NexiaRank.SUPPORTER)) {
                if(player.hasPermission("nexia.rank")) {
                    NexiaRank.removePrefix(NexiaRank.SUPPORTER, player);
                    return;
                }
                NexiaRank.setRank(NexiaRank.DEFAULT, player);
            }
            return;
        }

        Role supporterRole = jda.getRoleById("1107264322951979110");
        boolean hasRole = discordUser.getRoles().contains(supporterRole);
        boolean hasSupporterPrefix = player.hasPrefix(NexiaRank.SUPPORTER);

        if(hasRole && !hasSupporterPrefix) {
            if(player.hasPermission("nexia.rank")) {
                NexiaRank.addPrefix(NexiaRank.SUPPORTER, player, false);
                return;
            }
            NexiaRank.setRank(NexiaRank.SUPPORTER, player);
        } else if(!hasRole && hasSupporterPrefix) {
            if(player.hasPermission("nexia.rank")) {
                NexiaRank.removePrefix(NexiaRank.SUPPORTER, player);
                return;
            }
            NexiaRank.setRank(NexiaRank.DEFAULT, player);
        }
    }
}
