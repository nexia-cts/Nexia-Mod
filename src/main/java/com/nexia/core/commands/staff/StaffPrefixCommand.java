package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class StaffPrefixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {

        ArrayList<String> ranks = new ArrayList<>();
        NexiaRank.ranks.forEach(rank -> ranks.add(rank.id));

        dispatcher.register(Commands.literal("staffprefix")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.prefix"))
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"set", "add", "remove"}), builder)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("prefix", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((ranks), builder)))
                                        .executes(context -> {

                                            CommandSourceStack executor = context.getSource();

                                            String type = StringArgumentType.getString(context, "type");
                                            ServerPlayer mcOtherPlayer = EntityArgument.getPlayer(context, "player");
                                            Player otherPlayer = PlayerUtil.getFactoryPlayer(mcOtherPlayer);
                                            String prefix = StringArgumentType.getString(context, "prefix");

                                            ServerPlayer mcExecutor;
                                            Player factoryExecutor = null;

                                            try {
                                                mcExecutor = executor.getPlayerOrException();
                                                factoryExecutor = PlayerUtil.getFactoryPlayer(mcExecutor);
                                            } catch(Exception ignored){ }


                                            if(type.equalsIgnoreCase("set")){
                                                for(NexiaRank rank : NexiaRank.ranks){
                                                    if(prefix.equalsIgnoreCase(rank.id)){


                                                        if(factoryExecutor != null){
                                                            factoryExecutor.sendMessage(
                                                                    ChatFormat.nexiaMessage
                                                                                    .append(Component.text("You have set the prefix of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                                            .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2))
                                                                                                    .append(Component.text(" to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                                                            .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                                                                    .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false).decoration(ChatFormat.bold, false))
                                                            );
                                                        } else {
                                                            executor.sendSuccess(LegacyChatFormat.format("{b1}You have set the prefix of {b2}{} {b1}to: {b2}{b}{}{b1}.", otherPlayer.getRawName(), rank.name), false);
                                                        }

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

                                                        if(factoryExecutor != null){
                                                            factoryExecutor.sendMessage(
                                                                    ChatFormat.nexiaMessage
                                                                            .append(Component.text("You have removed the prefix ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(" from: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                            );
                                                        } else {
                                                            executor.sendSuccess(LegacyChatFormat.format("{b1}You have removed the prefix {b2}{b}{} {b1}from {b2}{}{b1}.", rank, otherPlayer.getRawName()), false);
                                                        }


                                                        ServerTime.factoryServer.runCommand(String.format("/lp user %s permission unset nexia.prefix.%s", otherPlayer.getRawName(), rank));
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("add")){
                                                for(NexiaRank rank : NexiaRank.ranks){
                                                    if(prefix.equalsIgnoreCase(rank.id)){
                                                        if(factoryExecutor != null){
                                                            factoryExecutor.sendMessage(
                                                                    ChatFormat.nexiaMessage
                                                                            .append(Component.text("You have added the prefix ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(rank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                                            .append(Component.text(" to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold,false))
                                                            );
                                                        } else {
                                                            executor.sendSuccess(LegacyChatFormat.format("{b1}You have added the prefix {b2}{b}{} {b1}to {b2}{}{b1}.", rank, otherPlayer.getRawName()), false);
                                                        }

                                                        ServerTime.factoryServer.runCommand(String.format("/lp user %s permission set nexia.prefix.%s true", otherPlayer.getRawName(), rank));
                                                    }
                                                }
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                ));

    }
}
