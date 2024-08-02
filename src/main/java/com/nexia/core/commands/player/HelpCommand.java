package com.nexia.core.commands.player;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import net.kyori.adventure.text.Component;

public class HelpCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("help").executes(HelpCommand::run));
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
            "stats" + commandSeparator + "shows you your stats",
            "duel" + commandSeparator + "duel a player that is in the hub (duels)",
            "customduel" + commandSeparator + "duel a player that is in the hub, in a custom kit that you made (custom duels)",
            "kitlayout" + commandSeparator + "edit your gamemode layout in duels",
            "kiteditor" + commandSeparator + "create kits for custom duels",
            "queue" + commandSeparator + "queue for a gamemode in duels",
            "spectate" + commandSeparator + "spectate a player which is in duels (or ffa if you have supporter rank)",
            "buy" + commandSeparator + "buy our ranks to support the server",
            "shout" + commandSeparator + "shout a message to the whole server (supporter)",
            "sprintfix" + commandSeparator + "toggle the sprint fix"
    };

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {

        Component message = ChatFormat.separatorLine("Commands");

        for (String command : commands) {
            String[] commandInfo = command.split(commandSeparator);
            if (commandInfo.length < 2) continue;

            message = message.append(Component.text("\n/" + commandInfo[0])
                    .color(ChatFormat.brandColor1)

                    .decoration(ChatFormat.strikeThrough, false)
                    .append(Component.text(" | ").color(ChatFormat.lineColor).decoration(ChatFormat.strikeThrough, false))
                    .append(Component.text(commandInfo[1], ChatFormat.brandColor2).decoration(ChatFormat.strikeThrough, false)));

            //message += "\n" + ChatFormat.brandColor1 + "/" + commandInfo[0] + ChatFormat.lineColor + " | " + ChatFormat.brandColor2 + commandInfo[1];
        }

        message = message.append(Component.text("\n").append(ChatFormat.separatorLine(null)));

        context.getSource().sendMessage(message);

        return 1;
    }
}
