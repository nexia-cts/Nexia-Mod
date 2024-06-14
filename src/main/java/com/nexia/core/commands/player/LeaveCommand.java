package com.nexia.core.commands.player;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;

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

    public static int run(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

        LobbyUtil.returnToLobby(player, true);
        return 1;
    }

}
