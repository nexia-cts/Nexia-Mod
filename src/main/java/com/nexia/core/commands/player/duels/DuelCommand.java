package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.Main;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class DuelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("duel")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.DUELS;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.duels), builder)))
                                .executes(context -> DuelCommand.challenge(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "gamemode"), null))
                                .then(Commands.argument("map", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Main.config.duelsMaps), builder)))
                                        .executes(context -> DuelCommand.challenge(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "gamemode"), StringArgumentType.getString(context, "map")))
        ))));
        dispatcher.register(Commands.literal("challenge")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.DUELS;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.duels), builder)))
                                .executes(context -> DuelCommand.challenge(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "gamemode"), null))
                                .then(Commands.argument("map", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Main.config.duelsMaps), builder)))
                                        .executes(context -> DuelCommand.challenge(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "gamemode"), StringArgumentType.getString(context, "map")))
        ))));
    }

    public static int challenge(CommandContext<CommandSourceStack> context, ServerPlayer player, String gameMode, @Nullable String map) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        GamemodeHandler.challengePlayer(executer, player, gameMode, map);
        return 1;
    }
}
