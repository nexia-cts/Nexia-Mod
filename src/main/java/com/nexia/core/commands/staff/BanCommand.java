package com.nexia.core.commands.staff;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

import java.util.Collection;

public class BanCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("ban")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", GameProfileArgument.gameProfile())
                        .executes(context -> BanCommand.ban(context.getSource(), context.getArgument("player", GameProfileArgument.Result.class).getNames(CommandUtil.getCommandSourceStack(context.getSource(), false)), "No reason specified."))
                        .then(CommandUtils.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> BanCommand.ban(context.getSource(), context.getArgument("player", GameProfileArgument.Result.class).getNames(CommandUtil.getCommandSourceStack(context.getSource(), false)), StringArgumentType.getString(context, "reason"))))
                )
        );
    }

    public static int ban(CommandSourceInfo context, Collection<GameProfile> collection, String reason) {
        UserBanList userBanList = ServerTime.minecraftServer.getPlayerList().getBans();
        int i = 0;


        for (GameProfile gameProfile : collection) {
            if (!userBanList.isBanned(gameProfile)) {
                ServerPlayer serverPlayer = ServerTime.minecraftServer.getPlayerList().getPlayer(gameProfile.getId());
                NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

                UserBanListEntry userBanListEntry = new UserBanListEntry(gameProfile, null, null, null, reason);
                userBanList.add(userBanListEntry);
                ++i;
                context.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("You have banned ", ChatFormat.normalColor))
                                .append(Component.text(ComponentUtils.getDisplayName(gameProfile).getString(), ChatFormat.brandColor2))
                                .append(Component.text(" for ", ChatFormat.normalColor))
                                .append(Component.text(reason, ChatFormat.brandColor2))
                );


                if (serverPlayer != null) {
                    nexiaPlayer.disconnect(Component.text("You have been banned.", ChatFormat.failColor)
                            .append(Component.text("\nReason: ", ChatFormat.systemColor))
                            .append(Component.text(reason, ChatFormat.brandColor2))
                            .append(Component.text("\nYou can appeal your ban at ", ChatFormat.systemColor))
                            .append(Component.text(NexiaDiscord.config.discordLink, ChatFormat.brandColor2))
                    );
                }
            }
        }

        if (i == 0) {
            context.sendMessage(
                    ChatFormat.nexiaMessage.append(Component.text("That player is already banned.", ChatFormat.failColor))
            );
        } else {
            return i;
        }


        return 1;
    }
}
