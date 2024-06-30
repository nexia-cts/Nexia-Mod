package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.gui.RanksGUI;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;

public class RanksCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("buy").executes(RanksCommand::run));
        dispatcher.register(CommandUtils.literal("store").executes(RanksCommand::run));
        dispatcher.register(CommandUtils.literal("ranks").executes(RanksCommand::run));
        dispatcher.register(CommandUtils.literal("role").executes(RanksCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        RanksGUI.openMainGUI(new NexiaPlayer(context.getSource().getPlayerOrException()).unwrap());
        return 1;
    }

}
