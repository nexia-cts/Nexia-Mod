package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;


public class MessageCommand {

    public static void registerMsg(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
        registerMsg(commandDispatcher, "msg");
        registerMsg(commandDispatcher, "message");
        registerMsg(commandDispatcher, "tell");
        registerMsg(commandDispatcher, "whisper");
    }

    public static void registerMsg(CommandDispatcher<CommandSourceStack> commandDispatcher, String string) {
        commandDispatcher.register(Commands.literal(string)
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::msgCommand)
                        )
                )
        );
    }



    public static void registerReply(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("r")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommand::replyCommand)
                ));
        dispatcher.register(Commands.literal("reply")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommand::replyCommand)
                )
        );
    }

    private static int msgCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer receiver = EntityArgument.getPlayer(context, "player");
        String message = StringArgumentType.getString(context, "message");

        sendMessage(sender, receiver, message);

        return 1;
    }

    private static int replyCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        PlayerData senderData = PlayerDataManager.get(sender);
        ServerPlayer receiver = PlayerUtil.getFixedPlayer(senderData.lastMessageSender);

        if (receiver == null) {
            sender.sendMessage(LegacyChatFormat.format("{s}Nobody to reply to"), Util.NIL_UUID);
            return 1;
        }

        String message = StringArgumentType.getString(context, "message");

        sendMessage(sender, receiver, message);
        return 1;
    }

    private static void sendMessage(ServerPlayer sender, ServerPlayer receiver, String message) {
        if (PlayerMutes.muted(sender) || sender == receiver) return;

        PlayerUtil.getNexusPlayer(sender).sendMessage(
                Component.text(String.format("To %s", receiver.getScoreboardName())).color(ChatFormat.brandColor2)
                                .append(Component.text(" » ").color(ChatFormat.arrowColor)
                                                .append(Component.text(message).color(ChatFormat.brandColor2))

        ));

        PlayerUtil.getNexusPlayer(receiver).sendMessage(
                Component.text(String.format("From %s", sender.getScoreboardName())).color(ChatFormat.brandColor2)
                        .append(Component.text(" » ").color(ChatFormat.arrowColor)
                                .append(Component.text(message).color(ChatFormat.brandColor2))

                        ));

        PlayerDataManager.get(receiver).lastMessageSender = sender;
    }

}
