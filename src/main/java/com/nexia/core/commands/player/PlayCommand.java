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
                .then(Commands.literal("skywars").executes(PlayCommand::playSkywars))
                .then(Commands.literal("sw").executes(PlayCommand::playSkywars))
                .then(Commands.literal("bedwars").executes(PlayCommand::playBedwars))
                .then(Commands.literal("bw").executes(PlayCommand::playBedwars))
                .then(Commands.literal("ffa").executes(PlayCommand::openGUI)
                        .then(Commands.literal("kits").executes(PlayCommand::playKitFFA))
                        .then(Commands.literal("pot").executes(PlayCommand::playPotFFA))
                        .then(Commands.literal("uhc").executes(PlayCommand::playUhcFFA))
                        .then(Commands.literal("classic").executes(PlayCommand::playNormalFFA)))
                .then(Commands.literal("duels").executes(PlayCommand::playDuels)));
        dispatcher.register(Commands.literal("join").executes(PlayCommand::openGUI)
                .then(Commands.literal("skywars").executes(PlayCommand::playSkywars))
                .then(Commands.literal("sw").executes(PlayCommand::playSkywars))
                .then(Commands.literal("bedwars").executes(PlayCommand::playBedwars))
                .then(Commands.literal("bw").executes(PlayCommand::playBedwars))
                .then(Commands.literal("ffa").executes(PlayCommand::openGUI)
                        .then(Commands.literal("pot").executes(PlayCommand::playPotFFA))
                        .then(Commands.literal("uhc").executes(PlayCommand::playUhcFFA))
                        .then(Commands.literal("kits").executes(PlayCommand::playKitFFA))
                        .then(Commands.literal("classic").executes(PlayCommand::playNormalFFA)))
                .then(Commands.literal("duels").executes(PlayCommand::playDuels))
        );
        dispatcher.register(Commands.literal("ffa").executes(PlayCommand::playNormalFFA));
    }

    private static int openGUI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        PlayGUI.openMainGUI(context.getSource().getPlayerOrException());
        return 1;
    }

    private static int playNormalFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "classic ffa", true, true);
        return 1;
    }

    private static int playUhcFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "uhc ffa", true, true);
        return 1;
    }

    private static int playPotFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "pot ffa", true, true);
        return 1;
    }

    private static int playKitFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "kits ffa", true, true);
        return 1;
    }

    private static int playBedwars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "bedwars", true, true);
        return 1;
    }

    private static int playSkywars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "skywars", true, true);
        return 1;
    }

    private static int playDuels(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(player, "duels", true, true);
        return 1;
    }

}
