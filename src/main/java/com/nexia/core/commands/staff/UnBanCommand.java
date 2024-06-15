package com.nexia.core.commands.staff;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;

import java.util.Collection;

public class UnBanCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("unban")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((context.getSource()).getServer().getPlayerList().getBans().getUserList(), builder)))
                        .executes(context -> UnBanCommand.unban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player")))
                )
        );
        dispatcher.register(Commands.literal("pardon")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((context.getSource()).getServer().getPlayerList().getBans().getUserList(), builder)))
                        .executes(context -> UnBanCommand.unban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player")))
                )
        );
    }

    public static int unban(CommandSourceStack context, Collection<GameProfile> collection) {
        UserBanList userBanList = ServerTime.minecraftServer.getPlayerList().getBans();
        int i = 0;

        ServerPlayer player = null;

        try {
            player = context.getPlayerOrException();
        } catch (Exception ignored){ }

        for (GameProfile gameProfile : collection) {
            if (userBanList.isBanned(gameProfile)) {
                userBanList.remove(gameProfile);
                ++i;
                if(player != null){
                    PlayerUtil.getFactoryPlayer(player).sendMessage(
                            ChatFormat.nexiaMessage
                                    .append(Component.text("You have unbanned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                            .append(Component.text(gameProfile.getName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                    .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))


                    );
                } else {
                    context.sendSuccess(LegacyChatFormat.format("{b1}You have unbanned {b2}{}{b1}.", gameProfile.getName()), true);
                }

            }
        }

        if (i == 0) {
            if(player != null){
                PlayerUtil.getFactoryPlayer(player).sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("That player is not banned.").color(ChatFormat.failColor))
                );
            } else {
                context.sendSuccess(LegacyChatFormat.formatFail("That player is not banned."), false);
            }

        } else {
            return i;
        }

        return 1;
    }
}
