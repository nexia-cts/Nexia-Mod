package com.nexia.core.commands.player;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.PlayGUI;
import com.nexia.core.utilities.commands.CommandUtil;

public class PlayCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        register(dispatcher, "play");
        register(dispatcher, "join");

        dispatcher.register(CommandUtils.literal("ffa").executes(PlayCommand::openFFAGui)
                .then(CommandUtils.literal("kits").executes(PlayCommand::playKitFFA))
                .then(CommandUtils.literal("sky").executes(PlayCommand::playSkyFFA))
                .then(CommandUtils.literal("uhc").executes(PlayCommand::playUhcFFA))
                .then(CommandUtils.literal("classic").executes(PlayCommand::playNormalFFA))
        );
    }

    private static void register(CommandDispatcher<CommandSourceInfo> dispatcher, String string) {
        dispatcher.register(CommandUtils.literal(string).executes(PlayCommand::openGUI)
                .then(CommandUtils.literal("ffa").executes(PlayCommand::openFFAGui)
                        .then(CommandUtils.literal("kits").executes(PlayCommand::playKitFFA))
                        .then(CommandUtils.literal("sky").executes(PlayCommand::playSkyFFA))
                        .then(CommandUtils.literal("uhc").executes(PlayCommand::playUhcFFA))
                        .then(CommandUtils.literal("classic").executes(PlayCommand::playNormalFFA)))
                .then(CommandUtils.literal("bedwars").executes(PlayCommand::playBedWars))
                .then(CommandUtils.literal("duels").executes(PlayCommand::playDuels))
                .then(CommandUtils.literal("skywars").executes(PlayCommand::playSkywars))
                .then(CommandUtils.literal("sw").executes(PlayCommand::playSkywars))
                .then(CommandUtils.literal("football").executes(PlayCommand::playFootball))
                .then(CommandUtils.literal("oitc").executes(PlayCommand::playOITC))
                .then(CommandUtils.literal("bw").executes(PlayCommand::playBedWars)));
    }


    private static int openGUI(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        PlayGUI.openMainGUI(CommandUtil.getPlayer(context).unwrap());
        return 1;
    }

    private static int openFFAGui(CommandContext<CommandSourceInfo> context){
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        PlayGUI.openMainGUI(CommandUtil.getPlayer(context).unwrap()).setFFALayout();
        return 1;
    }

    private static int playNormalFFA(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "classic ffa", true, true);
        return 1;
    }

    private static int playKitFFA(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "kits ffa", true, true);
        return 1;
    }

    private static int playUhcFFA(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "uhc ffa", true, true);
        return 1;
    }

    private static int playSkyFFA(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "sky ffa", true, true);
        return 1;
    }

    private static int playBedWars(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "bedwars", true, true);
        return 1;
    }

    private static int playDuels(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "duels", true, true);
        return 1;
    }

    private static int playOITC(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "oitc", true, true);
        return 1;
    }

    private static int playFootball(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "football", true, true);
        return 1;
    }

    private static int playSkywars(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        LobbyUtil.sendGame(CommandUtil.getPlayer(context), "skywars", true, true);
        return 1;
    }

}
