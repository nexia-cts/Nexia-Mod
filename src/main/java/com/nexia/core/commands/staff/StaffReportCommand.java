package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class StaffReportCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {

        dispatcher.register(Commands.literal("staffreport")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.report", 1))
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"ban", "unban"}), builder)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {

                                    CommandSourceStack executor = context.getSource();

                                    String type = StringArgumentType.getString(context, "type");
                                    ServerPlayer mcOtherPlayer = EntityArgument.getPlayer(context, "player");
                                    Player otherPlayer = PlayerUtil.getFactoryPlayer(mcOtherPlayer);

                                    Player factoryExecutor = null;

                                    try {
                                        factoryExecutor = PlayerUtil.getFactoryPlayer(executor.getPlayerOrException());
                                    } catch (Exception ignored) { }


                                    PlayerData data = PlayerDataManager.get(mcOtherPlayer);

                                    if(type.equalsIgnoreCase("ban")) {
                                        if(data.isReportBanned) {
                                            if(factoryExecutor != null) {
                                                factoryExecutor.sendMessage(Component.text("That player is already report banned!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false));
                                            } else {
                                                executor.sendFailure(new TextComponent("That player is already report banned!"));
                                            }
                                            return 0;
                                        }
                                        data.isReportBanned = true;
                                        if(factoryExecutor != null) {
                                            factoryExecutor.sendMessage(
                                                    ChatFormat.nexiaMessage
                                                            .append(Component.text("You have report banned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                                    .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                                                                    .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                            )
                                            );
                                        } else {
                                            executor.sendSuccess(LegacyChatFormat.format("You have report banned {}.", otherPlayer.getRawName()), true);
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(type.equalsIgnoreCase("unban") || type.equalsIgnoreCase("pardon")) {
                                        if(!data.isReportBanned) {
                                            if(factoryExecutor != null) {
                                                factoryExecutor.sendMessage(Component.text("That player is not report banned!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false));
                                            } else {
                                                executor.sendFailure(new TextComponent("That player is not report banned!"));
                                            }
                                            return 0;
                                        }
                                        data.isReportBanned = false;
                                        if(factoryExecutor != null) {
                                            factoryExecutor.sendMessage(
                                                    ChatFormat.nexiaMessage
                                                            .append(Component.text("You have report unbanned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                                    .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                                                                    .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                            )
                                            );
                                        } else {
                                            executor.sendSuccess(LegacyChatFormat.format("You have report unbanned {}.", otherPlayer.getRawName()), true);
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(factoryExecutor != null) {
                                        factoryExecutor.sendMessage(Component.text("Invalid argument").color(ChatFormat.failColor).decoration(ChatFormat.bold, false));
                                    } else {
                                        executor.sendFailure(new TextComponent("Invalid argument!"));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))

                )
        );

    }
}