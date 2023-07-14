package com.nexia.discord;

import com.nexia.discord.config.ModConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer {

    public static ModConfig config;

    public static JDA jda;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        Logger logger = com.nexia.core.Main.logger;

        logger.log(Level.INFO, "Initializing Nexia Discord...");

        jda = JDABuilder.createDefault(config.token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .build();

        logger.log(Level.INFO, "Successfully initialized Discord!");
        logger.log(Level.INFO, "Adding linking functionality...");
        jda.addEventListener(new Discord());
        Main.registerCommands();
        logger.log(Level.INFO, "Linking functionality has been added!");
    }

    public static void registerCommands() {
        jda.updateCommands().addCommands(
                Commands.slash("link", "Link the minecraft player with the discord player.")
                        .setGuildOnly(true)
                        .addOptions(
                                new OptionData(OptionType.STRING, "server", "Which server you're trying to link on.")
                                        .setRequired(true)
                                        .setMaxLength(3)
                                        .addChoice("EU", "eu")
                                        .addChoice("NA", "na")
                                        .addChoice("DEV", "dev"),
                                new OptionData(OptionType.INTEGER, "code", "The code when you do /link in minecraft.")
                                        .setRequired(true)
                                        .setRequiredRange(1000, 9999)
                        )
        ).queue();
    }
}
