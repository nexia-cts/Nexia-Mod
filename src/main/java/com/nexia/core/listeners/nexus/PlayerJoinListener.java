package com.nexia.core.listeners.nexus;

import com.nexia.ffa.classic.utilities.RatingUtil;
import com.nexia.nexus.api.event.player.PlayerJoinEvent;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.Main;
import com.nexia.discord.utilities.player.PlayerData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

import java.util.Objects;

import static com.nexia.discord.Main.jda;

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
                                        .append(Component.text(com.nexia.discord.Main.config.discordLink)
                                                .color(ChatFormat.brandColor2)
                                                .hoverEvent(HoverEvent.showText(Component.text("Click me").color(TextColor.fromHexString("#73ff54"))))
                                                .clickEvent(ClickEvent.openUrl(com.nexia.discord.Main.config.discordLink))
                                        )
        );
        player.sendMessage(ChatFormat.separatorLine(null));
    }

    private static void checkBooster(NexiaPlayer player) {
        PlayerData playerData = com.nexia.discord.utilities.player.PlayerDataManager.get(player.getUUID());
        if(!playerData.savedData.isLinked) { return; }
        Member discordUser = null;

        try {
            discordUser = Objects.requireNonNull(jda.getGuildById(Main.config.guildID)).retrieveMemberById(playerData.savedData.discordID).complete(true);
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
        if(PlayerDataManager.get(player).clientType.equals(com.nexia.core.utilities.player.PlayerData.ClientType.VIAFABRICPLUS)) return;

        com.nexia.ffa.classic.utilities.player.PlayerDataManager.addPlayerData(player);
        com.nexia.ffa.kits.utilities.player.PlayerDataManager.addPlayerData(player);
        com.nexia.ffa.uhc.utilities.player.PlayerDataManager.addPlayerData(player);
        com.nexia.ffa.sky.utilities.player.PlayerDataManager.addPlayerData(player);

        com.nexia.discord.utilities.player.PlayerDataManager.addPlayerData(player.getUUID());
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.addPlayerData(player);
        com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.addPlayerData(player);
        com.nexia.minigames.games.oitc.util.player.PlayerDataManager.addPlayerData(player);
        com.nexia.minigames.games.football.util.player.PlayerDataManager.addPlayerData(player);
        com.nexia.minigames.games.skywars.util.player.PlayerDataManager.addPlayerData(player);

        LobbyUtil.returnToLobby(player, true);

        checkBooster(player);
        RatingUtil.checkRatingRank(player);
        sendJoinMessage(player);
    }
}
