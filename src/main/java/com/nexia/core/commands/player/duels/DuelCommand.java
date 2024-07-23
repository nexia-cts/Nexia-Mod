package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.DuelGUI;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
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
                        NexiaPlayer player = new NexiaPlayer(commandSourceInfo.getPlayerOrException());

                        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
                        CorePlayerData playerData1 = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> {
                            NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

                            DuelGUI.openDuelGui(player.unwrap(), context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())));
                            return 1;
                        })
                        .then(CommandUtils.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.stringDuelGameModes), builder)))
                                .executes(context -> DuelCommand.challenge(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "gamemode"), null))
                                .then(CommandUtils.argument("map", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelsMap.stringDuelsMaps), builder)))
                                        .executes(context -> DuelCommand.challenge(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "gamemode"), StringArgumentType.getString(context, "map")))
                                ))));
    }

    public static int challenge(CommandContext<CommandSourceInfo> context, ServerPlayer player, String gameMode, @Nullable String map) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        GamemodeHandler.challengePlayer(executor, new NexiaPlayer(player), gameMode, DuelsMap.identifyMap(map));
        return 1;
    }
}