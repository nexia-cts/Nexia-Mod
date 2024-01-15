package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.gui.RanksGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RanksCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("buy").executes(RanksCommand::run));
        dispatcher.register(Commands.literal("store").executes(RanksCommand::run));
        dispatcher.register(Commands.literal("ranks").executes(RanksCommand::run));
        dispatcher.register(Commands.literal("role").executes(RanksCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        RanksGUI.openMainGUI(context.getSource().getPlayerOrException());
        return 1;
    }

}
