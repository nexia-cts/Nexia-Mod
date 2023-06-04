package com.nexia.discord;

import com.nexia.discord.config.ModConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {

    public static ModConfig config;

    public static JDA jda;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        jda = JDABuilder.createDefault(config.token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .build();

        Discord.registerCommands();
    }
}
