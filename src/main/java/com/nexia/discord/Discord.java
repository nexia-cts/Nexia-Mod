package com.nexia.discord;

import com.nexia.discord.commands.discord.base.BaseSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Discord extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        for(BaseSlashCommand slashCommand : BaseSlashCommand.slashCommands) {
            if(slashCommand.name.equalsIgnoreCase(command))  {
                slashCommand.commandTriggered(event);
                return;
            }
        }
    }
}