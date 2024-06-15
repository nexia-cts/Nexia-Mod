package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.UserBanList;

import java.util.Collection;

public class UnBanCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("unban")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", GameProfileArgument.gameProfile())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((ServerTime.minecraftServer.getPlayerList().getBans().getUserList()), builder)))
                        .executes(context -> UnBanCommand.unban(context.getSource(), context.getArgument("player", GameProfileArgument.Result.class).getNames(CommandUtil.getCommandSourceStack(context.getSource()))))
                )
        );
        dispatcher.register(CommandUtils.literal("pardon")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", GameProfileArgument.gameProfile())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((ServerTime.minecraftServer.getPlayerList().getBans().getUserList()), builder)))
                        .executes(context -> UnBanCommand.unban(context.getSource(), context.getArgument("player", GameProfileArgument.Result.class).getNames(CommandUtil.getCommandSourceStack(context.getSource()))))
                )
        );
    }

    public static int unban(CommandSourceInfo context, Collection<GameProfile> collection) {
        UserBanList userBanList = ServerTime.minecraftServer.getPlayerList().getBans();
        int i = 0;

        for (GameProfile gameProfile : collection) {
            if (userBanList.isBanned(gameProfile)) {
                userBanList.remove(gameProfile);
                ++i;

                context.getSender().sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("You have unbanned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(gameProfile.getName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                );

            }
        }

        if (i == 0) {
            context.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("That player is not banned.").color(ChatFormat.failColor))
            );

        } else {
            return i;
        }

        return 1;
    }
}
