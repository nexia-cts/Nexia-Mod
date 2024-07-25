package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class PartyCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        register(dispatcher, "party");
        register(dispatcher, "duelsteam");
    }

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher, String string) {
        dispatcher.register(CommandUtils.literal(string)
                .requires(commandSourceInfo -> {
                    try {
                        NexiaPlayer player = new NexiaPlayer(commandSourceInfo.getPlayerOrException());

                        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
                        CorePlayerData playerData1 = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(CommandUtils.literal("invite")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.invitePlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true)))))
                )
                .then(CommandUtils.literal("promote")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.promotePlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true)))))
                )
                .then(CommandUtils.literal("join")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.joinTeam(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true)))))
                )
                .then(CommandUtils.literal("kick")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.kickPlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true)))))
                )
                .then(CommandUtils.literal("decline")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.declinePlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true)))))
                )
                .then(CommandUtils.literal("disband").executes(PartyCommand::disbandTeam))
                .then(CommandUtils.literal("create").executes(PartyCommand::createTeam))
                .then(CommandUtils.literal("list").executes(PartyCommand::listTeam))
                .then(CommandUtils.literal("leave").executes(PartyCommand::leaveTeam))
        );
    }

    public static int invitePlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer invitor = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(invitor);

        if(data.duelOptions.duelsTeam == null) {
            DuelsTeam.createTeam(invitor, false);
        }
        data.duelOptions.duelsTeam.invitePlayer(invitor, new NexiaPlayer(player));
        return 1;
    }

    public static int joinTeam(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player.getUUID())).duelOptions.duelsTeam.joinTeam(executor);
        return 1;
    }

    public static int listTeam(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);

        if(data.duelOptions.duelsTeam == null) {
            executor.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        data.duelOptions.duelsTeam.listTeam(executor);
        return 1;
    }

    public static int declinePlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player.getUUID())).duelOptions.duelsTeam.declineTeam(executor);
        return 1;
    }

    public static int createTeam(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsTeam.createTeam(player, true);
        return 1;
    }

    public static int promotePlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);

        if(data.duelOptions.duelsTeam == null) {
            executor.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        data.duelOptions.duelsTeam.replaceLeader(executor, new NexiaPlayer(player), true);
        return 1;
    }

    public static int kickPlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);

        if(data.duelOptions.duelsTeam == null) {
            executor.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        data.duelOptions.duelsTeam.kickPlayer(executor, new NexiaPlayer(player));
        return 1;
    }

    public static int disbandTeam(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(data.duelOptions.duelsTeam == null) {
            player.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).duelOptions.duelsTeam.disbandTeam(player, true);
        return 1;
    }
    public static int leaveTeam(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(data.duelOptions.duelsTeam == null) {
            player.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).duelOptions.duelsTeam.leaveTeam(player, true);
        return 1;
    }
}
