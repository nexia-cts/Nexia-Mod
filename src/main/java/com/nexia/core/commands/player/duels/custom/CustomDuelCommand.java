package com.nexia.core.commands.player.duels.custom;

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
import com.nexia.core.gui.duels.CustomDuelGUI;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.item.InventoryUtil;
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

public class CustomDuelCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        register(dispatcher, "customduel");
        register(dispatcher, "customchallenge");
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

                            CustomDuelGUI.openDuelGui(player.unwrap(), context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())));
                            return 1;
                        })
                        .then(CommandUtils.argument("kit", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((InventoryUtil.getListOfInventories("duels/custom/" + context.getSource().getPlayerOrException().getUUID())), builder)))
                                .executes(context -> CustomDuelCommand.challenge(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "kit"), null))
                                .then(CommandUtils.argument("map", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelsMap.stringDuelsMaps), builder)))
                                        .executes(context -> CustomDuelCommand.challenge(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "kit"), StringArgumentType.getString(context, "map")))
                                ))));
    }

    public static int challenge(CommandContext<CommandSourceInfo> context, ServerPlayer player, String kit, @Nullable String map) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        GamemodeHandler.customChallengePlayer(executor, new NexiaPlayer(player), kit, DuelsMap.identifyMap(map));
        return 1;
    }
}