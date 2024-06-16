package com.nexia.core.utilities.ranks;

import com.combatreforged.factory.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NexiaRank {
    public static ArrayList<NexiaRank> ranks = new ArrayList<>();
    public String name;
    public final String id;
    public final String groupID;
    public final NexiaPlayerTeam team;

    NexiaRank(String name, String id, NexiaPlayerTeam team) {
        this.name = name;
        this.id = id;
        this.team = team;
        this.groupID = "group." + id;
        NexiaRank.ranks.add(this);
    }

    @Override
    public String toString() {
        return this.id;
    }

    public static class NexiaPlayerTeam {
        public final String name;
        public final String teamName;
        private final Component prefix;
        public final ChatFormatting color;

        public final String color1;
        public final String color2;

        public NexiaPlayerTeam(String name, String teamName, String color1, String color2, ChatFormatting color) {
            this.name = name;
            this.teamName = teamName;
            this.color = color;

            this.color1 = color1;
            this.color2 = color2;

            if(color1 == null || color2 == null) this.prefix = new TextComponent("");
            else {
                this.prefix = ObjectMappings.convertComponent(MiniMessage.get().parse(String.format("<bold><gradient:%s:%s>%s</gradient></bold> <dark_gray>| </dark_gray>", color1, color2, name)));
            }
        }

        @Override
        public String toString() {
            return this.teamName;
        }

        private @NotNull PlayerTeam getTeam(@NotNull MinecraftServer server) {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam playerTeam = scoreboard.getPlayerTeam(teamName);
            if (scoreboard.getPlayerTeam(this.teamName) == null) {
                playerTeam = scoreboard.addPlayerTeam(teamName);
            }
            playerTeam.setDisplayName(new TextComponent(name));
            playerTeam.setColor(this.color);
            playerTeam.setPlayerPrefix(this.prefix);
            return playerTeam;
        }
    }
    public static final NexiaRank DEFAULT = new NexiaRank(
            "Player",
            "default",
            new NexiaPlayerTeam("Player", "ZPlayer", null, null, ChatFormatting.GRAY)
    );
    public static final NexiaRank PRO = new NexiaRank(
            "Pro",
            "pro",
            new NexiaPlayerTeam("Pro", "ZAPro", "#A201F9", "#E401ED", ChatFormatting.WHITE)
    );
    public static final NexiaRank GOD = new NexiaRank(
            "God",
            "god",
            new NexiaPlayerTeam("God", "XYGod", "#ffb347", "#ffcc33", ChatFormatting.WHITE)
    );
    public static final NexiaRank SUPPORTER = new NexiaRank(
            "Nexia",
            "nexia",
            new NexiaPlayerTeam("Nexia", "XASupporter", ChatFormat.brandColor1.asHexString(), ChatFormat.brandColor2.asHexString(), ChatFormatting.WHITE)
    );
    public static final NexiaRank MEDIA = new NexiaRank(
            "Media",
            "media",
            new NexiaPlayerTeam("Media", "WZMedia", "#fc00c8", "#fc0000", ChatFormatting.WHITE)
    );
    public static final NexiaRank BUILDER = new NexiaRank(
            "Builder",
            "builder",
            new NexiaPlayerTeam("Builder", "WBuilder", "#01E7FE", "#014E56", ChatFormatting.WHITE)
    );
    public static final NexiaRank MOD = new NexiaRank(
            "Mod",
            "mod",
            new NexiaPlayerTeam("Mod", "VZMod", "#FFAA00", "#C18000", ChatFormatting.WHITE)
    );
    public static final NexiaRank DEV = new NexiaRank(
            "Dev",
            "dev",
            new NexiaPlayerTeam("Dev", "VDev", "#4CC9F0", "#4361EE", ChatFormatting.WHITE)
    );
    public static final NexiaRank FEMBOY = new NexiaRank(
            "Femboy",
            "femboy",
            new NexiaPlayerTeam("Femboy", "XBFemboy", "#FA6BC3", "#FDB3EB", ChatFormatting.WHITE)
    );
    public static final NexiaRank ADMIN = new NexiaRank(
            "Admin",
            "admin",
            new NexiaPlayerTeam("Admin", "VAdmin", "#BD2020", "#FD6C46", ChatFormatting.WHITE)
    );

    public static void setupRanks(MinecraftServer server) {
        NexiaRank.ranks.forEach(rank -> rank.team.getTeam(server));
    }

    public static NexiaRank identifyRank(@NotNull String rank) {
        if(rank.isBlank()) return null;

        for(NexiaRank nexiaRank : NexiaRank.ranks) {
            if(nexiaRank.id.equalsIgnoreCase(rank)) return nexiaRank;
        }
        return null;


    }

    public static void setRank(NexiaRank rank, ServerPlayer player) {
        for (NexiaRank rank1 : NexiaRank.ranks) {
            player.removeTag(rank1.id);
        }

        ServerTime.factoryServer.runCommand(String.format("/lp user %s parent set %s", player.getScoreboardName(), rank.id));
        player.addTag(rank.id);
    }

    public static void setPrefix(NexiaRank rank, ServerPlayer player) {
        for (NexiaRank rank1 : NexiaRank.ranks) {
            player.removeTag(rank1.id);
        }

        player.addTag(rank.id);
    }

    public static void removePrefix(NexiaRank rank, ServerPlayer player) {
        if(player.getTags().contains(rank.id)) {
            setPrefix(NexiaRank.DEFAULT, player);
        }

        ServerTime.factoryServer.runCommand(String.format("/lp user %s permission unset nexia.prefix.%s",
                player.getScoreboardName(), rank.id)
        );
    }

    public static void addPrefix(NexiaRank rank, ServerPlayer player, boolean setPrefix) {
        if(setPrefix) {
            setPrefix(rank, player);
        }

        ServerTime.factoryServer.runCommand(String.format("/lp user %s permission set nexia.prefix.%s",
                player.getScoreboardName(), rank.id)
        );
    }
}