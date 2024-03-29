package com.nexia.core.commands.player;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;

import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;


public class MessageCommand {

    public static void registerMsg(CommandDispatcher<CommandSourceInfo> commandDispatcher) {
        commandDispatcher.register(CommandUtils.literal("msg")
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::msgCommand)
                        )
                ));
        commandDispatcher.register(CommandUtils.literal("message")
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::msgCommand)
                        )
                )
        );
        commandDispatcher.register(CommandUtils.literal("tell")
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

    private static int msgCommand(CommandContext<CommandSourceInfo> context) {
        if(!PlayerUtil.checkPlayerInCommand(context)) return 1;
        sendMessage(PlayerUtil.getPlayer(context), PlayerUtil.getFactoryPlayer(context.getArgument("player", ServerPlayer.class)), StringArgumentType.getString(context, "message"));
        return 1;
    }

    private static int replyCommand(CommandContext<CommandSourceInfo> context) {
        if(!PlayerUtil.checkPlayerInCommand(context)) return 1;
        Player player = PlayerUtil.getPlayer(context);

        PlayerData senderData = PlayerDataManager.get(player);
        Player receiver = senderData.lastMessageSender;

        if (receiver == null) {
            player.sendMessage(Component.text("No one to reply to.").color(ChatFormat.systemColor));
            return 1;
        }

        String message = StringArgumentType.getString(context, "message");

        sendMessage(PlayerUtil.getPlayer(context), receiver, message);
        return 1;
    }

    private static void sendMessage(Player sender, Player receiver, String message) {
        if (PlayerMutes.muted(sender) || sender == receiver) return;

        sender.sendMessage(
                Component.text(String.format("To %s", receiver.getRawName())).color(ChatFormat.brandColor2)
                                .append(Component.text(" » ").color(ChatFormat.arrowColor)
                                                .append(Component.text(message).color(ChatFormat.brandColor2))

        ));

        sender.sendMessage(
                Component.text(String.format("From %s", sender.getRawName())).color(ChatFormat.brandColor2)
                        .append(Component.text(" » ").color(ChatFormat.arrowColor)
                                .append(Component.text(message).color(ChatFormat.brandColor2))

                        ));
        PlayerDataManager.get(receiver).lastMessageSender = sender;
    }

}
