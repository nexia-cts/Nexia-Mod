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
            "No hacking, cheating, griefing or exploiting bugs.",
            "Be respectful. No toxicity and/or annoying behaviour.",
            "No advertising.",
            "No encouraging of illegal activity.",
            "No interrupting other player's fights.",
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
