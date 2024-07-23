package com.nexia.core.commands.player;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.Main;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;


public class MessageCommand {

    public static void registerMsg(CommandDispatcher<CommandSourceInfo> commandDispatcher) {
        registerMsg(commandDispatcher, "msg");
        registerMsg(commandDispatcher, "message");
        registerMsg(commandDispatcher, "tell");
        registerMsg(commandDispatcher, "whisper");
    }

    public static void registerMsg(CommandDispatcher<CommandSourceInfo> commandDispatcher, String string) {
        commandDispatcher.register(CommandUtils.literal(string)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::msgCommand)
                        )
                )
        );
    }



    public static void registerReply(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("r")
                .then(CommandUtils.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommand::replyCommand)
                ));
        dispatcher.register(CommandUtils.literal("reply")
                .then(CommandUtils.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommand::replyCommand)
                )
        );
    }

    private static int msgCommand(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer sender = new NexiaPlayer(context.getSource().getPlayerOrException());
        NexiaPlayer receiver = new NexiaPlayer(context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())));
        String message = StringArgumentType.getString(context, "message");

        sendMessage(sender, receiver, message);

        return 1;
    }

    private static int replyCommand(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer sender = new NexiaPlayer(context.getSource().getPlayerOrException());
        CorePlayerData senderData = (CorePlayerData) PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(sender);
        NexiaPlayer receiver = senderData.lastMessageSender;

        if (receiver == null) {
            sender.sendMessage(Component.text("Nobody to reply to.", ChatFormat.systemColor));
            return 1;
        }

        String message = StringArgumentType.getString(context, "message");

        sendMessage(sender, receiver, message);
        return 1;
    }

    private static void sendMessage(NexiaPlayer sender, NexiaPlayer receiver, String message) {
        if (PlayerMutes.muted(sender) || sender.equals(receiver)) return;

        sender.sendMessage(
                Component.text(String.format("To %s", receiver.getRawName())).color(ChatFormat.brandColor2)
                                .append(Component.text(" » ").color(ChatFormat.arrowColor)
                                                .append(Component.text(message).color(ChatFormat.brandColor2))

        ));

        receiver.sendMessage(
                Component.text(String.format("From %s", sender.getRawName())).color(ChatFormat.brandColor2)
                        .append(Component.text(" » ").color(ChatFormat.arrowColor)
                                .append(Component.text(message).color(ChatFormat.brandColor2))

                        ));

        ((CorePlayerData) PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(receiver)).lastMessageSender = sender;
    }

}
