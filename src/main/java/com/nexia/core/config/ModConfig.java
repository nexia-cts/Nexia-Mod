package com.nexia.core.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "nexia-core")
public class ModConfig implements ConfigData {
    @Comment("The server commands that get run whenever a player leaves the server.")
    public String[] serverLeaveCommands = {""};
    @Comment("The player commands that get run whenever a player leaves the server.")
    public String[] playerLeaveCommands = {""};

    @Comment("The server commands that get run whenever a player joins the server.")
    public String[] serverJoinCommands = {""};
    @Comment("The player commands that get run whenever a player joins the server.")
    public String[] playerJoinCommands = {""};

    @Comment("The server commands that get run whenever a player joins the server for the first time.")
    public String[] serverFirstJoinCommands = {""};
    @Comment("The player commands that get run whenever a player joins the server for the first time.")
    public String[] playerFirstJoinCommands = {""};

    @Comment("What the discord link should be.")
    public String discordLink = "";

    @Comment("List of ranks.")
    public String[] ranks = {""};

    public double[] lobbyPos = {
            0.5,
            65.0,
            0.5,
    };

    @Comment("Toggles if the modified knockback is enabled.")
    public boolean modifiedKnockback = false;

    @Comment("Toggles if the modified fishing rods are enabled.")
    public boolean modifiedRods = false;

    @Comment("Toggles if the better shields are enabled.")
    public boolean betterShields = false;

    @Comment("Toggles if the join and leave messages show.")
    public boolean statusMessages = true;

    @Comment("The message that gets announced when the player joins the server for the first time.")
    public String firstJoinMessage = "WELCOME %player%";
    @Comment("The message that gets announced when the player joins the server.")
    public String joinMessage = "JOIN %player%";

    @Comment("The message that gets announced when the player leaves the server.")
    public String leaveMessage = "LEAVE %player%";

}
