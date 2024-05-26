package com.nexia.core.commands.player.duels;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.duels.QueueGUI;
import com.nexia.core.utilities.misc.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import net.minecraft.commands.SharedSuggestionProvider;

public class QueueCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("queue")
                .requires(commandSourceInfo -> {
                    try {
                        if(!CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        NexiaPlayer player = CommandUtil.getPlayer(commandSourceInfo);

                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
                        PlayerData playerData1 = PlayerDataManager.get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .executes(context -> {
                    if(!CommandUtil.failIfNoPlayerInCommand(context)) return 0;
                    NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

                    QueueGUI.openQueueGUI(player.unwrap());
                    return 1;
                })
                .then(CommandUtils.argument("gamemode", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((DuelGameMode.stringDuelGameModes), builder)))
                        .executes(context -> QueueCommand.queue(context, StringArgumentType.getString(context, "gamemode"))))
        );
    }

    public static int queue(CommandContext<CommandSourceInfo> context, String gameMode) throws CommandSyntaxException {
        if(!CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

        GamemodeHandler.joinQueue(player, gameMode, false);
        return 1;
    }
}
