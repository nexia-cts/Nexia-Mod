package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class PingCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("ping").executes(PingCommand::run)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> PingCommand.ping(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true))))
                )
        );
        dispatcher.register(CommandUtils.literal("latency").executes(PingCommand::run)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> PingCommand.ping(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true))))
                )
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        int ping = new NexiaPlayer(context.getSource().getPlayerOrException()).getLatency();

        context.getSource().sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("Your ping is ", ChatFormat.normalColor)
                                        .append(Component.text(ping + "ms", ChatFormat.brandColor2))
                                                .append(Component.text(".", ChatFormat.normalColor))
                                                        .append(Component.text(" (ping may not be accurate)", ChatFormat.Minecraft.dark_gray).decorate(ChatFormat.italic))
        ));

        return ping;
    }

    public static int ping(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        int ping = player.latency;

        context.getSource().sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("The ping of ", ChatFormat.normalColor))
                                        .append(Component.text(player.getScoreboardName(), ChatFormat.brandColor2))
                                                .append(Component.text(" is ", ChatFormat.normalColor))
                                                                        .append(Component.text(ping + "ms", ChatFormat.brandColor2))
                                                                                                .append(Component.text(".", ChatFormat.normalColor))
                                                                                                        .append(Component.text(" (ping may not be accurate)", ChatFormat.Minecraft.dark_gray).decorate(ChatFormat.italic))
        );

        return ping;
    }
}
