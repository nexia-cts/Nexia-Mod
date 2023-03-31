package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.PlayGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class PlayCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("play").executes(PlayCommand::openGUI)
                .then(Commands.literal("ffa").executes(PlayCommand::openGUI)
                        .then(Commands.literal("classic").executes(PlayCommand::playNormalFFA)))
                .then(Commands.literal("bedwars").executes(PlayCommand::playBedWars))
                .then(Commands.literal("duels").executes(PlayCommand::playDuels))
                .then(Commands.literal("oitc").executes(PlayCommand::playOITC))
                .then(Commands.literal("bw").executes(PlayCommand::playBedWars)));
        dispatcher.register(Commands.literal("join").executes(PlayCommand::openGUI)
                .then(Commands.literal("ffa").executes(PlayCommand::openGUI)
                        .then(Commands.literal("classic").executes(PlayCommand::playNormalFFA)))
                .then(Commands.literal("bedwars").executes(PlayCommand::playBedWars))
                .then(Commands.literal("duels").executes(PlayCommand::playDuels))
                .then(Commands.literal("bw").executes(PlayCommand::playBedWars))
                .then(Commands.literal("oitc").executes(PlayCommand::playOITC))
        );
        dispatcher.register(Commands.literal("ffa").executes(PlayCommand::playNormalFFA));
    }

    private static int openGUI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        PlayGUI.openMainGUI(context.getSource().getPlayerOrException());
        return 1;
    }

    private static int playNormalFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "classic ffa", true);
        return 1;
    }

    private static int playBedWars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "bedwars", true);
        return 1;
    }

    private static int playDuels(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "duels", true);
        return 1;
    }

    private static int playOITC(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "oitc", true);
        return 1;
    }

}
