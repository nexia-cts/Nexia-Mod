package com.nexia.discord;

import com.nexia.core.NexiaCore;
import com.nexia.discord.commands.discord.LinkSlashCommand;
import com.nexia.discord.config.ModConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class NexiaDiscord implements ModInitializer {

    public static ModConfig config;

    public static JDA jda;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        Logger logger = NexiaCore.logger;

        logger.log(Level.INFO, "Initializing Nexia Discord...");

        jda = JDABuilder.createDefault(config.token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .build();

        logger.log(Level.INFO, "Successfully initialized Discord!");
        logger.log(Level.INFO, "Registering slash commands...");
        jda.addEventListener(new Discord());
        NexiaDiscord.registerCommands();
        logger.log(Level.INFO, "Registered slash commands!");
    }

    public static void registerCommands() {
        new LinkSlashCommand();
    }
}
