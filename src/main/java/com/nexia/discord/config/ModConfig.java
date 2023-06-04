package com.nexia.discord.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "nexia-discord")
public class ModConfig implements ConfigData {
    public String token = "";

    public String guildID = "";

    @Comment("What the discord link should be.")
    public String discordLink = "";
}
