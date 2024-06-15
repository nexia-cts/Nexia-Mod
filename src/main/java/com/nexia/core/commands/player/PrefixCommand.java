package com.nexia.core.commands.player;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.gui.PrefixGUI;
import com.nexia.core.utilities.commands.CommandUtil;

public class PrefixCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("prefix").executes(PrefixCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        PrefixGUI.openRankGUI(CommandUtil.getPlayer(context).unwrap());
        return 1;
    }
}
