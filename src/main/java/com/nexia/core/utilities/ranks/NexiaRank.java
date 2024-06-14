package com.nexia.core.utilities.ranks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NexiaRank {
    public static ArrayList<NexiaRank> ranks = new ArrayList<>();

    public final String name;

    public final String id;

    public final String groupID;

    public final NexiaPlayerTeam team;

    private NexiaRank(String name, String id, NexiaPlayerTeam team) throws CommandSyntaxException {
        this.name = name;
        this.id = id;
        this.team = team;

        this.groupID = "group." + id;

        ranks.add(this);
    }



    public class NexiaPlayerTeam {

        public final String teamName;

        private final Component prefix;

        public final ChatFormatting color;

        private NexiaPlayerTeam(String teamName, Component prefix, ChatFormatting color) {
            this.teamName = teamName;
            this.prefix = prefix;
            this.color = color;

            getTeam();
        }

        private @NotNull PlayerTeam getTeam() {
            ServerScoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
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

    public NexiaRank DEFAULT = new NexiaRank(
            "Player",
            "default",
            new NexiaPlayerTeam("ZPlayer", new TextComponent(""), ChatFormatting.GRAY)
    );

    public NexiaRank SUPPORTER = new NexiaRank(
            "Nexia",
            "supporter",
            new NexiaPlayerTeam("XASupporter", ComponentArgument.textComponent().parse(new StringReader("[{\"text\":\"N\",\"color\":\"#A600FF\",\"bold\":true},{\"text\":\"e\",\"color\":\"#BC00FB\",\"bold\":true},{\"text\":\"x\",\"color\":\"#D300F7\",\"bold\":true},{\"text\":\"i\",\"color\":\"#EA00F3\"},{\"text\":\"a\",\"color\":\"#FF00EF\"},{\"text\":\" | \",\"color\":\"dark_gray\",\"bold\":false}]")), ChatFormatting.WHITE)
    );

    public NexiaRank MEDIA = new NexiaRank(
            "Media",
            "media",
            new NexiaPlayerTeam("WZMedia", ComponentArgument.textComponent().parse(new StringReader("[{\"text\":\"M\",\"color\":\"#fc00c8\",\"bold\":true},{\"text\":\"e\",\"color\":\"#e8008a\",\"bold\":true},{\"text\":\"d\",\"color\":\"#fc0064\",\"bold\":true},{\"text\":\"i\",\"color\":\"#fc0032\"},{\"text\":\"a\",\"color\":\"#fc0000\"},{\"text\":\" | \",\"color\":\"dark_gray\",\"bold\":false}]")), ChatFormatting.WHITE)
    );

    public NexiaRank BUILDER = new NexiaRank(
            "Builder",
            "builder",
            new NexiaPlayerTeam("WBuilder", ComponentArgument.textComponent().parse(new StringReader("[{\"text\":\"B\",\"color\":\"#01E7FE\",\"bold\":true},{\"text\":\"u\",\"color\":\"#01D6EB\",\"bold\":true},{\"text\":\"i\",\"color\":\"#02D1E6\",\"bold\":true},{\"text\":\"l\",\"color\":\"#02BFD2\",\"bold\":true},{\"text\":\"d\",\"color\":\"#03A1AF\",\"bold\":true},{\"text\":\"e\",\"color\":\"#02737E\",\"bold\":true},{\"text\":\"r\",\"color\":\"#014E56\"},{\"text\":\" | \",\"color\":\"dark_gray\",\"bold\":false}]")), ChatFormatting.WHITE)
    );

    public NexiaRank MOD = new NexiaRank(
            "Mod",
            "mod",
            new NexiaPlayerTeam("VZMod", ComponentArgument.textComponent().parse(new StringReader("[{\"text\":\"M\",\"color\":\"#FFAA00\",\"bold\":true},{\"text\":\"o\",\"color\":\"#D48D00\",\"bold\":true},{\"text\":\"d\",\"color\":\"#C18000\",\"bold\":true},{\"text\":\" | \",\"color\":\"dark_gray\",\"bold\":false}]")), ChatFormatting.WHITE)
    );

    public NexiaRank DEV = new NexiaRank(
            "Dev",
            "dev",
            new NexiaPlayerTeam("VDev", ComponentArgument.textComponent().parse(new StringReader("[{\"text\":\"D\",\"color\":\"#4CC9F0\",\"bold\":true},{\"text\":\"e\",\"color\":\"#4895EF\",\"bold\":true},{\"text\":\"v\",\"color\":\"#4361EE\",\"bold\":true},{\"text\":\" | \",\"color\":\"dark_gray\",\"bold\":false}]\n")), ChatFormatting.WHITE)
    );

    public NexiaRank ADMIN = new NexiaRank(
            "Admin",
            "admin",
            new NexiaPlayerTeam("VAdmin", ComponentArgument.textComponent().parse(new StringReader("[{\"text\":\"A\",\"color\":\"#BD2020\",\"bold\":true},{\"text\":\"d\",\"color\":\"#CD332A\",\"bold\":true},{\"text\":\"m\",\"color\":\"#DD4633\",\"bold\":true},{\"text\":\"i\",\"color\":\"#ED593D\",\"bold\":true},{\"text\":\"n\",\"color\":\"#FD6C46\",\"bold\":true},{\"text\":\" | \",\"color\":\"dark_gray\",\"bold\":false}]")), ChatFormatting.WHITE)
    );
}
