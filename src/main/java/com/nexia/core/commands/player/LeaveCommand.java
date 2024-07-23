package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;

public class LeaveCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        register(dispatcher, "leave");
        register(dispatcher, "lobby");
        register(dispatcher, "l");
        register(dispatcher, "hub");
    }

    private static void register(CommandDispatcher<CommandSourceInfo> dispatcher, String string) {
        dispatcher.register(CommandUtils.literal(string).executes(LeaveCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());
        LobbyUtil.returnToLobby(player, true);
        return 1;
    }

}
