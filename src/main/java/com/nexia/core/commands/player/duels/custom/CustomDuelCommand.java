package com.nexia.core.commands.player.duels.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.CustomDuelGUI;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class CustomDuelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        register(dispatcher, "customduel");
        register(dispatcher, "customchallenge");
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, String string) {
        dispatcher.register(Commands.literal(string)
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> CustomDuelGUI.openDuelGui(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player")))
                        .then(Commands.argument("kit", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((InventoryUtil.getListOfInventories("duels/custom/" + context.getSource().getPlayerOrException().getStringUUID())), builder)))
                                .executes(context -> CustomDuelCommand.challenge(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "kit"), null))
                                .then(Commands.argument("map", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelsMap.stringDuelsMaps), builder)))
                                        .executes(context -> CustomDuelCommand.challenge(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "kit"), StringArgumentType.getString(context, "map")))
                                ))));
    }

    public static int challenge(CommandContext<CommandSourceStack> context, ServerPlayer player, String kit, @Nullable String map) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        GamemodeHandler.customChallengePlayer(executor, player, kit, DuelsMap.identifyMap(map));
        return 1;
    }
}