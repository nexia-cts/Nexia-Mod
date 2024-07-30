package com.nexia.discord;

import com.nexia.discord.commands.discord.base.BaseSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Discord extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(BaseSlashCommand.slashCommands.isEmpty()) return;
        String command = event.getName();

        for(BaseSlashCommand slashCommand : BaseSlashCommand.slashCommands) {
            if(slashCommand.name.equalsIgnoreCase(command))  {
                slashCommand.commandTriggered(event);
                return;
            }
        }
    }
}