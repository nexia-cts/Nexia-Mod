package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class StaffReportCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        dispatcher.register(CommandUtils.literal("staffreport")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.report", 3))
                .then(CommandUtils.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"ban", "unban"}), builder)))
                        .then(CommandUtils.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    String type = StringArgumentType.getString(context, "type");
                                    ServerPlayer mcOtherPlayer = context.getArgument("player", ServerPlayer.class);
                                    NexiaPlayer otherPlayer = new NexiaPlayer(mcOtherPlayer);


                                    PlayerData data = PlayerDataManager.get(mcOtherPlayer.getUUID());

                                    if(type.equalsIgnoreCase("ban")) {
                                        if(data.savedData.isReportBanned()) {
                                            context.getSource().sendMessage(Component.text("That player is already report banned!").color(ChatFormat.failColor));
                                            return 0;
                                        }
                                        data.savedData.setReportBanned(true);
                                        context.getSource().sendMessage(
                                                ChatFormat.nexiaMessage
                                                        .append(Component.text("You have report banned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                                .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                        )
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(type.equalsIgnoreCase("unban") || type.equalsIgnoreCase("pardon")) {
                                        if(!data.savedData.isReportBanned()) {
                                            context.getSource().sendMessage(Component.text("That player is not report banned!").color(ChatFormat.failColor));
                                            return 0;
                                        }
                                        data.savedData.setReportBanned(false);
                                        context.getSource().sendMessage(
                                                ChatFormat.nexiaMessage
                                                        .append(Component.text("You have report unbanned ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                                .append(Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                        )
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    context.getSource().sendMessage(Component.text("Invalid argument").color(ChatFormat.failColor));

                                    return 0;
                                }))

                )
        );

    }
}