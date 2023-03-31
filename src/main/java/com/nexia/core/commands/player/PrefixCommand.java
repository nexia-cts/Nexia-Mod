package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.gui.PrefixGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class PrefixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("prefix").executes(PrefixCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        PrefixGUI.openRankGUI(executer);
        return 1;
    }
}
