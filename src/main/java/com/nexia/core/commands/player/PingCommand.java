package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import net.minecraft.Util;
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

    public static int calculate(int ping){
        if(ping < 4){
            return -1;
        }
        return ping;
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        int ping = executer.latency;

        if(calculate(ping) == -1){
            executer.sendMessage(ChatFormat.format("{b1}Your ping is {b2}INVALID{b1}." ), Util.NIL_UUID);
        } else {
            executer.sendMessage(ChatFormat.format("{b1}Your ping is {b2}{}ms{b1}.", ping), Util.NIL_UUID);
        }

        return ping;
    }

    public static int ping(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        CommandSourceStack executer = context.getSource();
        int ping = player.latency;

        if(calculate(ping) == -1){
            executer.sendSuccess(ChatFormat.format("{b1}The ping of {} is {b2}INVALID{b1}.", player.getScoreboardName()), false);
        } else {
            executer.sendSuccess(ChatFormat.format("{b1}The ping of {} is {b2}{}ms{b1}.", player.getScoreboardName(), ping), false);
        }

        return ping;
    }
}
