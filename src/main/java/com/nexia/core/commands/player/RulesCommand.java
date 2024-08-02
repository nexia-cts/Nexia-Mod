package com.nexia.core.commands.player;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import net.kyori.adventure.text.Component;

public class RulesCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("rules").executes(RulesCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) {

        /*
        Apparently this just doesn't work.....
        Component message = ChatFormat.separatorLine("Rules");
         */


        // Doing it the manual way 😎
        Component message = Component.text("")
                .append(Component.text("                          ").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough, true))
                .append(Component.text("[ ").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough, false)
                        .append(Component.text("Rules", ChatFormat.brandColor1).decoration(ChatFormat.strikeThrough, false))
                        .append(Component.text(" ]").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough, false))
                        .append(Component.text("                           ").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough, true)));

        for (int i = 0; i < NexiaCore.config.rules.length; i++) {

            message = message.append(Component.text("\n" + (i+1) + ". ")
                    .color(ChatFormat.brandColor1)
                    .decoration(ChatFormat.bold, true))
                    .decoration(ChatFormat.strikeThrough, false)
                    .append(Component.text("» ")
                            .color(ChatFormat.arrowColor)
                            
                            .decoration(ChatFormat.strikeThrough, false)
                            .append(Component.text(NexiaCore.config.rules[i])
                                    
                                    .decoration(ChatFormat.strikeThrough, false)
                                    .color(ChatFormat.normalColor)
                            )
                    );
        }

        message = message.append(Component.text("\n").append(ChatFormat.separatorLine(null)));

        context.getSource().sendMessage(message);

        return 1;
    }
}
