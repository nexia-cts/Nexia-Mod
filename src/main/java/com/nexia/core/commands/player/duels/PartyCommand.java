package com.nexia.core.commands.player.duels;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.team.DuelsTeam;
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
                        if(!CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        NexiaPlayer player = CommandUtil.getPlayer(commandSourceInfo);

                        assert player != null;
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
                        PlayerData playerData1 = PlayerDataManager.get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(CommandUtils.literal("invite")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.invitePlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())))))
                )
                .then(CommandUtils.literal("promote")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.promotePlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())))))
                )
                .then(CommandUtils.literal("join")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.joinTeam(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())))))
                )
                .then(CommandUtils.literal("kick")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.kickPlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())))))
                )
                .then(CommandUtils.literal("decline")
                        .then(CommandUtils.argument("player", EntityArgument.player()).executes(context -> PartyCommand.declinePlayer(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())))))
                )
                .then(CommandUtils.literal("disband").executes(PartyCommand::disbandTeam))
                .then(CommandUtils.literal("create").executes(PartyCommand::createTeam))
                .then(CommandUtils.literal("list").executes(PartyCommand::listTeam))
                .then(CommandUtils.literal("leave").executes(PartyCommand::leaveTeam))
        );
    }

    public static int invitePlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer invitor = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(invitor);

        if(data.duelOptions.duelsTeam == null) {
            DuelsTeam.createTeam(invitor, false);
        }
        data.duelOptions.duelsTeam.invitePlayer(invitor, new NexiaPlayer(player));
        return 1;
    }

    public static int joinTeam(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player.getUUID()).duelOptions.duelsTeam.joinTeam(executor);
        return 1;
    }

    public static int listTeam(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(executor);

        if(data.duelOptions.duelsTeam == null) {
            PlayerUtil.getFactoryPlayer(executor).sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        data.duelOptions.duelsTeam.listTeam(executor);
        return 1;
    }

    public static int declinePlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player.getUUID()).duelOptions.duelsTeam.declineTeam(executor);
        return 1;
    }

    public static int createTeam(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

        DuelsTeam.createTeam(player, true);
        return 1;
    }

    public static int promotePlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(executor);

        if(data.duelOptions.duelsTeam == null) {
            executor.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        data.duelOptions.duelsTeam.replaceLeader(executor, new NexiaPlayer(player), true);
        return 1;
    }

    public static int kickPlayer(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(executor);

        if(data.duelOptions.duelsTeam == null) {
            executor.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        data.duelOptions.duelsTeam.kickPlayer(executor, new NexiaPlayer(player));
        return 1;
    }

    public static int disbandTeam(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);

        if(data.duelOptions.duelsTeam == null) {
            player.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).duelOptions.duelsTeam.disbandTeam(player, true);
        return 1;
    }
    public static int leaveTeam(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);

        if(data.duelOptions.duelsTeam == null) {
            player.sendMessage(Component.text("You aren't in a team!").color(ChatFormat.failColor));
            return 1;
        }

        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).duelOptions.duelsTeam.leaveTeam(player, true);
        return 1;
    }
}
