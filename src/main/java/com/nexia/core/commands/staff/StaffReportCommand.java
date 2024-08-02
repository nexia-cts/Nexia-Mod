package com.nexia.core.commands.staff;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
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
                                    ServerPlayer mcOtherPlayer = context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), false));
                                    NexiaPlayer otherPlayer = new NexiaPlayer(mcOtherPlayer);

                                    CorePlayerData data = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(mcOtherPlayer.getUUID());

                                    if(type.equalsIgnoreCase("ban")) {
                                        if(((CoreSavedPlayerData)data.savedData).isReportBanned()) {
                                            context.getSource().sendMessage(Component.text("That player is already report banned!", ChatFormat.failColor));
                                            return 0;
                                        }
                                        ((CoreSavedPlayerData)data.savedData).setReportBanned(true);
                                        context.getSource().sendMessage(
                                                ChatFormat.nexiaMessage
                                                        .append(Component.text("You have report banned ", ChatFormat.normalColor)
                                                                .append(Component.text(otherPlayer.getRawName(), ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".", ChatFormat.normalColor))
                                                        )
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(type.equalsIgnoreCase("unban") || type.equalsIgnoreCase("pardon")) {
                                        if(!((CoreSavedPlayerData)data.savedData).isReportBanned()) {
                                            context.getSource().sendMessage(Component.text("That player is not report banned!", ChatFormat.failColor));
                                            return 0;
                                        }
                                        ((CoreSavedPlayerData)data.savedData).setReportBanned(false);
                                        context.getSource().sendMessage(
                                                ChatFormat.nexiaMessage
                                                        .append(Component.text("You have report unbanned ", ChatFormat.normalColor))
                                                                .append(Component.text(otherPlayer.getRawName(), ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                                                                .append(Component.text(".", ChatFormat.normalColor))
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    context.getSource().sendMessage(Component.text("Invalid argument", ChatFormat.failColor));

                                    return 0;
                                }))

                )
        );

    }
}