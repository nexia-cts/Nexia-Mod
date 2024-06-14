package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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

public class RankCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        ArrayList<String> arrayListRank = new ArrayList<>();
        NexiaRank.ranks.forEach(rank -> arrayListRank.add(rank.id));

        dispatcher.register(CommandUtils.literal("rank")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.rank"))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("rank", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((arrayListRank), builder)))
                                .executes(context -> RankCommand.give(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "rank")))))
        );
    }

    public static int give(CommandContext<CommandSourceInfo> context, ServerPlayer player, String rank) {
        NexiaPlayer otherNexiaPlayer = new NexiaPlayer(player);

        NexiaRank nexiaRank = NexiaRank.identifyRank(rank);
        if(nexiaRank == null) {
            context.getSource().sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Invalid rank!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
            );
            return 0;
        }
        
        context.getSource().sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("You have set the rank of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(otherNexiaPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                .append(Component.text(" to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(nexiaRank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                .append(Component.text(".").color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false).decoration(ChatFormat.bold, false))
                );


        otherNexiaPlayer.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("Your rank has been set to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                        .append(Component.text(nexiaRank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
        );

        NexiaRank.setRank(nexiaRank, player);

        return 1;
    }
}
