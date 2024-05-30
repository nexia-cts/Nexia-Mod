package com.nexia.core.commands.player;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class PingCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("ping").executes(PingCommand::run)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> PingCommand.ping(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource()))))
                )
        );
        dispatcher.register(CommandUtils.literal("latency").executes(PingCommand::run)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> PingCommand.ping(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource()))))
                )
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        int ping = CommandUtil.getPlayer(context).unwrap().latency;

        context.getSource().sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("Your ping is ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                        .append(Component.text(ping + "ms").color(ChatFormat.brandColor2))
                                                .append(Component.text(".").color(ChatFormat.normalColor))
                                                        .append(Component.text(" (ping may not be accurate)").color(NamedTextColor.GRAY).decorate(ChatFormat.italic))
        ));

        return ping;
    }

    public static int ping(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        int ping = player.latency;

        context.getSource().sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("The ping of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                        .append(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                .append(Component.text(" is ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(ping + "ms").color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                                                .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                                                        .append(Component.text(" (ping may not be accurate)").color(NamedTextColor.GRAY).decorate(ChatFormat.italic).decoration(ChatFormat.bold, false))
        );

        return ping;
    }
}
