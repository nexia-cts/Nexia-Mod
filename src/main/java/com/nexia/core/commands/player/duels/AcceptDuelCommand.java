package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class AcceptDuelCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        register(dispatcher, "acceptduel");
        register(dispatcher, "acceptchallenge");
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
                        .executes(context -> AcceptDuelCommand.accept(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true))))
                )
        );
    }


    public static int accept(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        GamemodeHandler.acceptDuel(executor, new NexiaPlayer(player));
        return 1;
    }
}
