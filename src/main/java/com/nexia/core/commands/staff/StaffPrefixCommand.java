package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                                            ServerPlayer mcOtherPlayer = context.getArgument("player", ServerPlayer.class);
                                            NexiaPlayer otherPlayer = new NexiaPlayer(mcOtherPlayer);
                                            String prefix = StringArgumentType.getString(context, "prefix");

                                            if(type.equalsIgnoreCase("set")){
                                                for(NexiaRank rank : NexiaRank.ranks){
                                                    if(prefix.equalsIgnoreCase(rank.id)){


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

                                                        for (NexiaRank tRank : NexiaRank.ranks) {
                                                            otherPlayer.removeTag(tRank.id);
                                                        }

                                                        otherPlayer.addTag(rank.id);
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("remove")){
                                                for(NexiaRank rank : NexiaRank.ranks){
                                                    if(prefix.equalsIgnoreCase(rank.id)){

                                                        context.getSource().sendMessage(
                                                                ChatFormat.nexiaMessage
                                                                        .append(Component.text("You have removed the prefix ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(" from: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                        );


                                                        ServerTime.metisServer.runCommand(String.format("/lp user %s permission unset %s", otherPlayer.getRawName(), rank.groupID));
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("add")){
                                                for(NexiaRank rank : NexiaRank.ranks){
                                                    if(prefix.equalsIgnoreCase(rank.id)){
                                                        context.getSource().sendMessage(
                                                                ChatFormat.nexiaMessage
                                                                        .append(Component.text("You have added the prefix ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                        .append(Component.text(" to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                        );

                                                        ServerTime.metisServer.runCommand(String.format("/lp user %s permission set %s true", otherPlayer.getRawName(), rank.groupID));
                                                    }
                                                }
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                ));

    }
}
