package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.ranks.NexiaRank;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class StaffPrefixCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        ArrayList<String> ranks = new ArrayList<>();
        NexiaRank.ranks.forEach(rank -> ranks.add(rank.id));

        dispatcher.register(CommandUtils.literal("staffprefix")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.prefix"))
                .then(CommandUtils.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"set", "add", "remove"}), builder)))
                        .then(CommandUtils.argument("player", EntityArgument.player())
                                .then(CommandUtils.argument("prefix", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((ranks), builder)))
                                        .executes(context -> {

                                            String type = StringArgumentType.getString(context, "type");
                                            ServerPlayer mcOtherPlayer = context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource()));
                                            NexiaPlayer otherPlayer = new NexiaPlayer(mcOtherPlayer);
                                            NexiaRank rank = NexiaRank.identifyRank(StringArgumentType.getString(context, "prefix"));

                                            if(rank == null) {
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("Invalid rank!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                );
                                                return 0;
                                            }

                                            if(type.equalsIgnoreCase("set")){
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("You have set the prefix of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2))
                                                                .append(Component.text(" to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false).decoration(ChatFormat.bold, false))
                                                );

                                                otherPlayer.sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("Your prefix has been set to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                );

                                                NexiaRank.setPrefix(rank, otherPlayer);
                                            }

                                            if(type.equalsIgnoreCase("remove")){
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("You have removed the prefix ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(" from: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                );

                                                NexiaRank.removePrefix(rank, otherPlayer);
                                            }

                                            if(type.equalsIgnoreCase("add")){
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("You have added the prefix ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(" to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                );
                                                NexiaRank.addPrefix(rank, otherPlayer, false);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                ));

    }
}
