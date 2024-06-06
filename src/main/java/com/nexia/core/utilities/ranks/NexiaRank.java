package com.nexia.core.utilities.ranks;

import com.combatreforged.factory.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.chat.ChatFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
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

    public static class NexiaPlayerTeam {

        public final String name;

        public final String teamName;

        private final Component prefix;

        public final ChatFormatting color;

        public NexiaPlayerTeam(String name, String teamName, String color1, String color2, ChatFormatting color) {
            this.name = name;
            this.teamName = teamName;
            this.color = color;

            if(color1 == null || color2 == null) this.prefix = new TextComponent("");
            else {
                this.prefix = ObjectMappings.convertComponent(MiniMessage.get().parse(String.format("<bold><gradient:%s:%s>%s</gradient></bold> <dark_gray>| </dark_gray>", color1, color2, name)));
            }
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
            new NexiaPlayerTeam("Pro", "ZAPro", "#360033", "#2E1437", ChatFormatting.WHITE)
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

    public static final NexiaRank ADMIN = new NexiaRank(
            "Admin",
            "admin",
            new NexiaPlayerTeam("Admin", "VAdmin", "#BD2020", "#FD6C46", ChatFormatting.WHITE)
    );

    public static void setupRanks(MinecraftServer server) {
        NexiaRank.ranks.forEach(rank -> rank.team.getTeam(server));
    }
}