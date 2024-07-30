package com.nexia.discord.commands.discord.base;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.nexia.discord.NexiaDiscord.jda;

public class BaseSlashCommand {

    public String name;

    public String description;

    OptionData[] optionData;

    public static ArrayList<BaseSlashCommand> slashCommands;

    public BaseSlashCommand(String name, String description, @NotNull OptionData... optionData) {
        this.name = name;
        this.description = description;
        this.optionData = optionData;

        slashCommands.add(this);
        this.register();
    }

    public void register() {
        jda.updateCommands().addCommands(
                Commands.slash(this.name, this.description)
                        .setGuildOnly(true)
                        .addOptions(this.optionData)
        ).queue();
    }

    public void commandTriggered(SlashCommandInteractionEvent event) {
        this.sendMessage("This command hasn't been fully implemented yet!", event);
    }

    public void sendMessage(String message, SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        event.getHook().editOriginal(message).queue();
    }
}
