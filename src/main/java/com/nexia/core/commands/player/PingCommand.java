package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class PingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("ping").executes(PingCommand::run)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> PingCommand.ping(context, EntityArgument.getPlayer(context, "player")))
                )
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        int ping = executer.latency;

        PlayerUtil.getFactoryPlayer(executer).sendMessage(ChatFormat.returnAppendedComponent(
                ChatFormat.nexiaMessage(),
                Component.text("Your ping is ").color(ChatFormat.normalColor),
                Component.text(ping + "ms").color(ChatFormat.brandColor2),
                Component.text(".").color(ChatFormat.normalColor),
                Component.text(" (ping may not be accurate)").color(ChatFormat.systemColor).decorate(ChatFormat.italic)
        ));

        return ping;
    }

    public static int ping(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        CommandSourceStack cmdExecutor = context.getSource();
        ServerPlayer executor;
        int ping = player.latency;

        try {
            executor = cmdExecutor.getPlayerOrException();
        } catch(Exception ignored){
            cmdExecutor.sendSuccess(LegacyChatFormat.format("{b1}The ping of {} is {b2}{}ms{b1}.", player.getScoreboardName(), ping), false);
            return ping;
        }

        PlayerUtil.getFactoryPlayer(executor).sendMessage(ChatFormat.returnAppendedComponent(
                ChatFormat.nexiaMessage(),
                Component.text("The ping of ").color(ChatFormat.normalColor),
                Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2),
                Component.text(" is ").color(ChatFormat.normalColor),
                Component.text(ping + "ms").color(ChatFormat.brandColor2),
                Component.text(".").color(ChatFormat.normalColor),
                Component.text(" (ping may not be accurate)").color(ChatFormat.systemColor).decorate(ChatFormat.italic)
        ));

        return ping;
    }
}
