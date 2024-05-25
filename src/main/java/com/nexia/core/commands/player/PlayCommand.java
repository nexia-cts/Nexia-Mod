package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.PlayGUI;
import com.nexia.core.utilities.player.NexiaPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.players.AccuratePlayer;

public class PlayCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {

        register(dispatcher, "play");
        register(dispatcher, "join");

        dispatcher.register(Commands.literal("ffa").executes(PlayCommand::openFFAGui)
                .then(Commands.literal("kits").executes(PlayCommand::playKitFFA))
                .then(Commands.literal("sky").executes(PlayCommand::playSkyFFA))
                .then(Commands.literal("uhc").executes(PlayCommand::playUhcFFA))
                .then(Commands.literal("classic").executes(PlayCommand::playNormalFFA))
        );
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, String string) {
        dispatcher.register(Commands.literal(string).executes(PlayCommand::openGUI)
                .then(Commands.literal("ffa").executes(PlayCommand::openFFAGui)
                        .then(Commands.literal("kits").executes(PlayCommand::playKitFFA))
                        .then(Commands.literal("sky").executes(PlayCommand::playSkyFFA))
                        .then(Commands.literal("uhc").executes(PlayCommand::playUhcFFA))
                        .then(Commands.literal("classic").executes(PlayCommand::playNormalFFA)))
                .then(Commands.literal("bedwars").executes(PlayCommand::playBedWars))
                .then(Commands.literal("duels").executes(PlayCommand::playDuels))
                .then(Commands.literal("skywars").executes(PlayCommand::playSkywars))
                .then(Commands.literal("sw").executes(PlayCommand::playSkywars))
                .then(Commands.literal("football").executes(PlayCommand::playFootball))
                .then(Commands.literal("oitc").executes(PlayCommand::playOITC))
                .then(Commands.literal("bw").executes(PlayCommand::playBedWars)));
    }


    private static int openGUI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        PlayGUI.openMainGUI(context.getSource().getPlayerOrException());
        return 1;
    }

    private static int openFFAGui(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        PlayGUI.openMainGUI(context.getSource().getPlayerOrException()).setFFALayout();
        return 1;
    }

    private static int playNormalFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "classic ffa", true, true);
        return 1;
    }

    private static int playKitFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "kits ffa", true, true);
        return 1;
    }

    private static int playUhcFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "uhc ffa", true, true);
        return 1;
    }

    private static int playSkyFFA(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "sky ffa", true, true);
        return 1;
    }

    private static int playBedWars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "bedwars", true, true);
        return 1;
    }

    private static int playDuels(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "duels", true, true);
        return 1;
    }

    private static int playOITC(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "oitc", true, true);
        return 1;
    }

    private static int playFootball(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "football", true, true);
        return 1;
    }

    private static int playSkywars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LobbyUtil.sendGame(new NexiaPlayer(new AccuratePlayer(player)), "skywars", true, true);
        return 1;
    }

}
