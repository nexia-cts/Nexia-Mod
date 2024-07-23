package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.QueueGUI;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.minecraft.commands.SharedSuggestionProvider;

public class QueueCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("queue")
                .requires(commandSourceInfo -> {
                    try {
                        NexiaPlayer player = new NexiaPlayer(commandSourceInfo.getPlayerOrException());

                        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
                        CorePlayerData playerData1 = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .executes(context -> {
                    NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

                    QueueGUI.openQueueGUI(player.unwrap());
                    return 1;
                })
                .then(CommandUtils.argument("gamemode", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.stringDuelGameModes), builder)))
                        .executes(context -> QueueCommand.queue(context, StringArgumentType.getString(context, "gamemode"))))
        );
    }

    public static int queue(CommandContext<CommandSourceInfo> context, String gameMode) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        GamemodeHandler.joinQueue(player, gameMode, false);
        return 1;
    }
}
