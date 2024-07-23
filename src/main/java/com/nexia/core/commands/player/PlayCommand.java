package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.PlayGUI;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;

public class PlayCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        register(dispatcher, "play");
        register(dispatcher, "join");

        dispatcher.register(CommandUtils.literal("ffa").executes(PlayCommand::openFFAGui)
                .then(CommandUtils.literal("kits").executes(context -> playGame(context, "kits ffa")))
                .then(CommandUtils.literal("sky").executes(context -> playGame(context, "sky ffa")))
                .then(CommandUtils.literal("uhc").executes(context -> playGame(context, "uhc ffa")))
                .then(CommandUtils.literal("classic").executes(context -> playGame(context, "classic ffa")))
        );
    }

    private static void register(CommandDispatcher<CommandSourceInfo> dispatcher, String string) {
        dispatcher.register(CommandUtils.literal(string).executes(PlayCommand::openGUI)
                .then(CommandUtils.literal("ffa").executes(PlayCommand::openFFAGui)
                        .then(CommandUtils.literal("kits").executes(context -> playGame(context, "kits ffa")))
                        .then(CommandUtils.literal("sky").executes(context -> playGame(context, "sky ffa")))
                        .then(CommandUtils.literal("uhc").executes(context -> playGame(context, "uhc ffa")))
                        .then(CommandUtils.literal("classic").executes(context -> playGame(context, "classic ffa")))
                )
                .then(CommandUtils.literal("bedwars").executes(context -> playGame(context, "bedwars")))
                .then(CommandUtils.literal("duels").executes(context -> playGame(context, "duels")))
                .then(CommandUtils.literal("skywars").executes(context -> playGame(context, "skywars")))
                .then(CommandUtils.literal("sw").executes(context -> playGame(context, "skywars")))
                .then(CommandUtils.literal("football").executes(context -> playGame(context, "football")))
                .then(CommandUtils.literal("oitc").executes(context -> playGame(context, "oitc")))
                .then(CommandUtils.literal("bw").executes(context -> playGame(context, "bedwars")))
        );
    }

    private static int openGUI(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        PlayGUI.openMainGUI(new NexiaPlayer(context.getSource().getPlayerOrException()).unwrap());
        return 1;
    }

    private static int openFFAGui(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        PlayGUI.openMainGUI(new NexiaPlayer(context.getSource().getPlayerOrException()).unwrap()).setFFALayout();
        return 1;
    }

    private static int playGame(CommandContext<CommandSourceInfo> context, String game) throws CommandSyntaxException {
        return playGame(new NexiaPlayer(context.getSource().getPlayerOrException()), game);
    }

    private static int playGame(NexiaPlayer player, String game) {
        LobbyUtil.sendGame(player, game, true, true);
        return 1;
    }
}
