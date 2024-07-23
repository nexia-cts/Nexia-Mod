package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.gui.PrefixGUI;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;

public class PrefixCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("prefix").executes(PrefixCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        PrefixGUI.openRankGUI(new NexiaPlayer(context.getSource().getPlayerOrException()).unwrap());
        return 1;
    }
}
