package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class RulesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("rules").executes(RulesCommand::run));
    }

    private static final String[] rules = {
            "No cheats or other unfair advantages",
            "No racism, excessive spamming or hate speech",
            "Don't abuse any bugs and glitches",
            "Keep it G, PG and PG-13",
            "No interrupting",
    };

    public static int run(CommandContext<CommandSourceStack> context) {

        String message = ChatFormat.separatorLine("Rules");
        for (int i = 0; i < rules.length; i++) {
            message += "\n" + "\247d" + ChatFormat.bold + (i+1) + ". §8» " + ChatFormat.normalColor + rules[i];
        }
        message += "\n" + ChatFormat.separatorLine(null);

        context.getSource().sendSuccess(new TextComponent(message), false);

        return 1;
    }
}
