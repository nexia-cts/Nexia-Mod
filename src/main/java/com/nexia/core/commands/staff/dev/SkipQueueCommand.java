package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.Main;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SkipQueueCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("skipqueue")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.skipqueue", 1)))
                .executes(SkipQueueCommand::currentGamemode)
                .then(Commands.literal("oitc").executes(SkipQueueCommand::oitc))
                .then(Commands.literal("sw").executes(SkipQueueCommand::skywars))
                .then(Commands.literal("bw").executes(SkipQueueCommand::bedwars))
                .then(Commands.literal("football").executes(SkipQueueCommand::football))
        );
    }

    private static int currentGamemode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerGameMode gameMode = ((CorePlayerData)PlayerDataManager.getDataManager(Main.CORE_DATA_MANAGER).get(player.getUUID())).gameMode;

        if(gameMode.equals(PlayerGameMode.SKYWARS)) return skywars(context);
        if(gameMode.equals(PlayerGameMode.BEDWARS)) return bedwars(context);
        if(gameMode.equals(PlayerGameMode.OITC)) return oitc(context);
        if(gameMode.equals(PlayerGameMode.FOOTBALL)) return football(context);

        player.sendMessage(LegacyChatFormat.formatFail("No gamemode found!"), Util.NIL_UUID);
        return 0;
    }

    private static int skywars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (SkywarsGame.isStarted || SkywarsGame.queueTime <= 1) {
            player.sendMessage(new TextComponent("Epic queue skip failure"), Util.NIL_UUID);
        } else {
            SkywarsGame.queueTime = 1;
            player.sendMessage(new TextComponent("Skipped queue"), Util.NIL_UUID);
        }

        return 1;
    }

    private static int football(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (FootballGame.isStarted || FootballGame.queueTime <= 1) {
            player.sendMessage(new TextComponent("Epic queue skip failure"), Util.NIL_UUID);
        } else {
            FootballGame.queueTime = 1;
            player.sendMessage(new TextComponent("Skipped queue"), Util.NIL_UUID);
        }

        return 1;
    }


    private static int oitc(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (OitcGame.isStarted || OitcGame.queueTime <= 1) {
            player.sendMessage(new TextComponent("Epic queue skip failure"), Util.NIL_UUID);
        } else {
            OitcGame.queueTime = 1;
            player.sendMessage(new TextComponent("Skipped queue"), Util.NIL_UUID);
        }

        return 1;
    }

    private static int bedwars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (BwUtil.skipQueue()) {
            player.sendMessage(new TextComponent("Skipped queue"), Util.NIL_UUID);
        } else {
            player.sendMessage(new TextComponent("Epic queue skip failure"), Util.NIL_UUID);
        }

        return 1;
    }
}