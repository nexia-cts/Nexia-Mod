package com.nexia.core.commands.staff;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

import java.util.Collection;
import java.util.Date;

public class BanCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("ban")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(context -> BanCommand.ban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player"), "No reason specified."))
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> BanCommand.ban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player"), StringArgumentType.getString(context, "reason"))))
                )
        );
    }

    public static int ban(CommandSourceStack context, Collection<GameProfile> collection, String reason) {
        UserBanList userBanList = Main.server.getPlayerList().getBans();
        int i = 0;

        ServerPlayer player = null;

        try {
            player = context.getPlayerOrException();
        } catch (Exception ignored){ }

        for (GameProfile gameProfile : collection) {
            if (!userBanList.isBanned(gameProfile)) {
                ServerPlayer serverPlayer = Main.server.getPlayerList().getPlayer(gameProfile.getId());

                UserBanListEntry userBanListEntry = new UserBanListEntry(gameProfile, (Date) null, context.getTextName(), (Date) null, reason);
                userBanList.add(userBanListEntry);
                ++i;
                if(player != null){

                    PlayerUtil.getFactoryPlayer(player).sendMessage(
                            ChatFormat.nexiaMessage()
                                            .append(Component.text("You have banned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                    .append(Component.text(ComponentUtils.getDisplayName(gameProfile).getString()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                            .append(Component.text(" for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                    .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                    );

                } else {
                    context.sendSuccess(LegacyChatFormat.format("{b1}You have banned {b2}{} {b1}for {b2}{}", ComponentUtils.getDisplayName(gameProfile).getString(), reason), true);
                }

                if (serverPlayer != null) {
                    serverPlayer.connection.disconnect(new TextComponent("§c§lYou have been banned.\n§7Reason: §d" + reason + "\n§7You can appeal your ban at §d" + Main.config.discordLink));
                }
            }
        }

        if (i == 0) {
            if(player != null){
                PlayerUtil.getFactoryPlayer(player).sendMessage(
                        ChatFormat.nexiaMessage().append(Component.text("That player is already banned.").color(ChatFormat.failColor))
                );
            } else {
                context.sendFailure(LegacyChatFormat.formatFail("That player is already banned."));
            }

        } else {
            return i;
        }


        return 1;
    }
}
