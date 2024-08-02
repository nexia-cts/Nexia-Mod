package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
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
                                            ServerPlayer mcOtherPlayer = context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), false));
                                            NexiaPlayer otherPlayer = new NexiaPlayer(mcOtherPlayer);
                                            NexiaRank rank = NexiaRank.identifyRank(StringArgumentType.getString(context, "prefix"));

                                            if(rank == null) {
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("Invalid rank!", ChatFormat.normalColor))
                                                );
                                                return 0;
                                            }

                                            if(type.equalsIgnoreCase("set")){
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("You have set the prefix of ", ChatFormat.normalColor))
                                                                .append(Component.text(otherPlayer.getRawName(), ChatFormat.brandColor2))
                                                                .append(Component.text(" to: ", ChatFormat.normalColor))
                                                                .append(Component.text(rank.name, ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".", ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                );

                                                otherPlayer.sendNexiaMessage(
                                                        Component.text("Your prefix has been set to: ", ChatFormat.normalColor)
                                                                .append(Component.text(rank.name, ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".", ChatFormat.normalColor))
                                                );

                                                NexiaRank.setPrefix(rank, otherPlayer);
                                            }

                                            if(type.equalsIgnoreCase("remove")){
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("You have removed the prefix ", ChatFormat.normalColor))
                                                                .append(Component.text(rank.name, ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(" from: ", ChatFormat.normalColor))
                                                                .append(Component.text(otherPlayer.getRawName(), ChatFormat.brandColor2))
                                                                .append(Component.text(".", ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                );

                                                NexiaRank.removePrefix(rank, otherPlayer);
                                            }

                                            if(type.equalsIgnoreCase("add")){
                                                context.getSource().sendMessage(
                                                        ChatFormat.nexiaMessage
                                                                .append(Component.text("You have added the prefix ", ChatFormat.normalColor))
                                                                .append(Component.text(rank.name, ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(" to: ", ChatFormat.normalColor))
                                                                .append(Component.text(otherPlayer.getRawName(), ChatFormat.brandColor2))
                                                                .append(Component.text(".", ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                );
                                                NexiaRank.addPrefix(rank, otherPlayer, false);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                ));

    }
}
