package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class RankCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        ArrayList<String> ranks = new ArrayList<>();
        NexiaRank.ranks.forEach(rank -> ranks.add(rank.id));

        dispatcher.register(CommandUtils.literal("rank")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.rank"))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("rank", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((ranks), builder)))
                                .executes(context -> RankCommand.give(context, context.getArgument("player", ServerPlayer.class), StringArgumentType.getString(context, "rank")))))
        );
    }

    public static int give(CommandContext<CommandSourceInfo> context, ServerPlayer player, String rank) {
        NexiaPlayer otherFactoryPlayer = new NexiaPlayer(player);

        for(NexiaRank tRank : NexiaRank.ranks){
            if(rank.equalsIgnoreCase(tRank.id)){

                context.getSource().sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("You have set the rank of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(otherFactoryPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                .append(Component.text(" to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(tRank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                .append(Component.text(".").color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false).decoration(ChatFormat.bold, false))
                );


                otherFactoryPlayer.sendMessage(
                        ChatFormat.nexiaMessage
                                        .append(Component.text("Your rank has been set to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                .append(Component.text(tRank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                );

                for (NexiaRank tRank2 : NexiaRank.ranks) {
                    otherFactoryPlayer.removeTag(tRank2.id);
                }

                ServerTime.metisServer.runCommand(String.format("/lp user %s parent set %s", otherFactoryPlayer.getRawName(), tRank.id));

                otherFactoryPlayer.addTag(tRank.id);
            }
        }

        return 1;
    }
}
