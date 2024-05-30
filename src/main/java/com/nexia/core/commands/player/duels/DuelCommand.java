package com.nexia.core.commands.player.duels;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.DuelGUI;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class DuelCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        register(dispatcher, "duel");
        register(dispatcher, "challenge");
    }

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher, String string) {
        dispatcher.register(CommandUtils.literal(string)
                .requires(commandSourceInfo -> {
                    try {
                        if(!CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(commandSourceInfo));

                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
                        PlayerData playerData1 = PlayerDataManager.get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> {
                            if(!CommandUtil.failIfNoPlayerInCommand(context)) return 0;
                            NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

                            DuelGUI.openDuelGui(player.unwrap(), context.getArgument("player", ServerPlayer.class));
                            return 1;
                        })
                        .then(CommandUtils.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.stringDuelGameModes), builder)))
                                .executes(context -> DuelCommand.challenge(context, context.getArgument("player", ServerPlayer.class), StringArgumentType.getString(context, "gamemode"), null))
                                .then(CommandUtils.argument("map", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelsMap.stringDuelsMaps), builder)))
                                        .executes(context -> DuelCommand.challenge(context, context.getArgument("player", ServerPlayer.class), StringArgumentType.getString(context, "gamemode"), StringArgumentType.getString(context, "map")))
                                ))));
    }

    public static int challenge(CommandContext<CommandSourceInfo> context, ServerPlayer player, String gameMode, @Nullable String map) throws CommandSyntaxException {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = new NexiaPlayer(CommandUtil.getPlayer(context));

        GamemodeHandler.challengePlayer(executor, new NexiaPlayer(player), gameMode, DuelsMap.identifyMap(map));
        return 1;
    }
}