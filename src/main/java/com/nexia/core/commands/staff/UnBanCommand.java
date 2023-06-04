package com.nexia.core.commands.staff;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.UserBanList;

import java.util.Collection;

public class UnBanCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("unban")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((context.getSource()).getServer().getPlayerList().getBans().getUserList(), builder)))
                        .executes(context -> UnBanCommand.unban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player")))
                )
        );
        dispatcher.register(Commands.literal("pardon")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((context.getSource()).getServer().getPlayerList().getBans().getUserList(), builder)))
                        .executes(context -> UnBanCommand.unban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player")))
                )
        );
    }

    public static int unban(CommandSourceStack context, Collection<GameProfile> collection) throws CommandSyntaxException {
        UserBanList userBanList = Main.server.getPlayerList().getBans();
        int i = 0;

        for (GameProfile gameProfile : collection) {
            if (userBanList.isBanned(gameProfile)) {
                userBanList.remove(gameProfile);
                ++i;
                context.sendSuccess(ChatFormat.format("{b1}You have unbanned {b2}{}{b1}.", gameProfile.getName()), true);
            }
        }

        if (i == 0) {
            context.sendSuccess(ChatFormat.formatFail("That player is not banned."), false);
        } else {
            return i;
        }

        return 1;
    }
}
