package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.QueueGUI;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.players.AccuratePlayer;

public class QueueCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("queue")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException().getUUID());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException().getUUID());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .executes(context -> QueueGUI.openQueueGUI(context.getSource().getPlayerOrException()))
                .then(Commands.argument("gamemode", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.stringDuelGameModes), builder)))
                        .executes(context -> QueueCommand.queue(context, StringArgumentType.getString(context, "gamemode"))))
        );
    }

    public static int queue(CommandContext<CommandSourceStack> context, String gameMode) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        GamemodeHandler.joinQueue(new NexiaPlayer(new AccuratePlayer(executor)), gameMode, false);
        return 1;
    }
}
