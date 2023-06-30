package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.commands.player.PlayCommand;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class TeamCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("duels-team")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> TeamCommand.invitePlayer(context, EntityArgument.getPlayer(context, "player"))))
                )
                .then(Commands.literal("join")
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> TeamCommand.joinTeam(context, EntityArgument.getPlayer(context, "player"))))
                )
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> TeamCommand.kickPlayer(context, EntityArgument.getPlayer(context, "player"))))
                )
                .then(Commands.literal("disband").executes(TeamCommand::disbandTeam))
                .then(Commands.literal("create").executes(TeamCommand::createTeam))
                .then(Commands.literal("leave").executes(TeamCommand::leaveTeam))
        );
    }

    public static int invitePlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        ServerPlayer invitor = context.getSource().getPlayerOrException();

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(invitor);

        if(data.duelsTeam == null) DuelsTeam.createTeam(player, false);
        data.duelsTeam.invitePlayer(invitor, player);
        return 1;
    }

    public static int joinTeam(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(executor);
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).duelsTeam.joinTeam(context.getSource().getPlayerOrException());
        return 1;
    }

    public static int createTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        DuelsTeam.createTeam(context.getSource().getPlayerOrException(), true);
        return 1;
    }

    public static int kickPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(executor);

        if(data.duelsTeam == null) {
            PlayerUtil.getFactoryPlayer(executor).sendMessage(Component.text("You aren't in a team!"));
            return 1;
        }

        data.duelsTeam.kickPlayer(executor, player);
        return 1;
    }

    public static int disbandTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);

        if(data.duelsTeam == null) {
            PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You aren't in a team!"));
            return 1;
        }

        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).duelsTeam.disbandTeam(player, true);
        return 1;
    }
    public static int leaveTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);

        if(data.duelsTeam == null) {
            PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You aren't in a team!"));
            return 1;
        }

        com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).duelsTeam.leaveTeam(player, true);
        return 1;
    }
}
