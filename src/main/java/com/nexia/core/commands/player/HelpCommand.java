package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class HelpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("help").executes(HelpCommand::run));
    }

    static String commandSeparator = ": ";

    static String[] commands = {
            "join" + commandSeparator + "join a game",
            "leave" + commandSeparator + "leave a game",
            "rules" + commandSeparator + "rules of the server",
            "discord" + commandSeparator + "leads you to our discord",
            "ping" + commandSeparator + "shows a player's ping",
            "prefix" + commandSeparator + "allows you to select your prefix",
            "report" + commandSeparator + "allows you to report other players",
            "msg" + commandSeparator + "allows you to message other players",
            "stats" + commandSeparator + "shows you your stats"
    };

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Component message = ChatFormat.separatorLine("Commands");

        for (int i = 0; i < commands.length; i++) {
            String[] commandInfo = commands[i].split(commandSeparator);
            if (commandInfo.length < 2) continue;

            message.append(Component.text("\n/" + commandInfo[0]).color(ChatFormat.brandColor1))
                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                    .append(Component.text(commandInfo[1]).color(ChatFormat.brandColor2));

            //message += "\n" + ChatFormat.brandColor1 + "/" + commandInfo[0] + ChatFormat.lineColor + " | " + ChatFormat.brandColor2 + commandInfo[1];
        }

        message.append(ChatFormat.separatorLine(null));

        PlayerUtil.getFactoryPlayer(context.getSource().getPlayerOrException()).sendMessage(message);

        return 1;
    }
}
