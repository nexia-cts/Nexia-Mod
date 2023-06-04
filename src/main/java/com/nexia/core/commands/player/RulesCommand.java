package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
            "No spamming/filling up chat.",
            "No stalling/combat logging."
    };

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        /*
         * Apparently this just doesn't work.....
         * Component message = ChatFormat.separatorLine("Rules");
         */

        // Doing it the manual way 😎
        Component message = Component.text("")
                .append(Component.text("                          ").color(ChatFormat.lineColor)
                        .decoration(ChatFormat.strikeThrough, true))
                .append(Component.text("[ ").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough, false)
                        .append(Component.text("Rules").color(ChatFormat.brandColor1)
                                .decoration(ChatFormat.strikeThrough, false))
                        .append(Component.text(" ]").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough,
                                false))
                        .append(Component.text("                           ").color(ChatFormat.lineColor)
                                .decoration(ChatFormat.strikeThrough, true)));

        for (int i = 0; i < rules.length; i++) {

            message = message.append(Component.text("\n" + (i + 1) + ". ")
                    .color(ChatFormat.brandColor1)
                    .decoration(ChatFormat.bold, true))
                    .decoration(ChatFormat.strikeThrough, false)
                    .append(Component.text("» ")
                            .color(ChatFormat.arrowColor)
                            .decoration(ChatFormat.bold, false)
                            .decoration(ChatFormat.strikeThrough, false)
                            .append(Component.text(rules[i])
                                    .decoration(ChatFormat.bold, false)
                                    .decoration(ChatFormat.strikeThrough, false)
                                    .color(ChatFormat.normalColor)));
        }

        message = message.append(Component.text("\n").append(ChatFormat.separatorLine(null)));

        PlayerUtil.getFactoryPlayer(context.getSource().getPlayerOrException()).sendMessage(message);

        return 1;
    }
}
