package com.nexia.core.listeners.nexus;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.nexus.api.event.player.PlayerJoinEvent;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

import java.util.Objects;

import static com.nexia.discord.NexiaDiscord.jda;

public class PlayerJoinListener {
    public void registerListener() {
        PlayerJoinEvent.BACKEND.register(playerJoinEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerJoinEvent.getPlayer());
            processJoin(player);

            /*
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
                                                        .append(Component.text(ServerTime.nexusServer.getMaxPlayerCount()).color(ChatFormat.brandColor2))
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
                                        .append(Component.text(NexiaDiscord.config.discordLink)
                                                .color(ChatFormat.brandColor2)
                                                .hoverEvent(HoverEvent.showText(Component.text("Click me").color(TextColor.fromHexString("#73ff54"))))
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
            if(player.hasPermission("nexia.prefix.supporter")) {
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
        boolean hasSupporterPrefix = player.hasPermission("nexia.prefix.supporter");

        if(hasRole && !hasSupporterPrefix) {
            if(player.hasPermission("nexia.rank")) {
                NexiaRank.addPrefix(NexiaRank.SUPPORTER, player, true);
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

    private static void processJoin(NexiaPlayer player) {
        if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).clientType.equals(CorePlayerData.ClientType.VIAFABRICPLUS)) return;

        PlayerDataManager.dataManagerMap.forEach((resourceLocation, playerDataManager) -> playerDataManager.addPlayerData(player));

        LobbyUtil.returnToLobby(player, true);

        checkBooster(player);
        sendJoinMessage(player);
    }
}
