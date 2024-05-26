package com.nexia.core.commands.player;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.discord.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("discord").executes(DiscordCommand::run));
        dispatcher.register(CommandUtils.literal("dc").executes(DiscordCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        context.getSource().sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("Link to discord: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                        .append(Component.text(com.nexia.discord.Main.config.discordLink).color(ChatFormat.brandColor2)
                                                .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.greenColor)))
                                                .clickEvent(ClickEvent.openUrl(Main.config.discordLink)))
        ));

        return 1;
    }

}
