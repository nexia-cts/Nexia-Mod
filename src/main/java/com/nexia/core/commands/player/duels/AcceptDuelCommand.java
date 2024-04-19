package com.nexia.core.commands.player.duels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class AcceptDuelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        register(dispatcher, "acceptduel");
        register(dispatcher, "acceptchallenge");
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
                        .executes(context -> AcceptDuelCommand.accept(context, EntityArgument.getPlayer(context, "player")))
                )
        );
    }


    public static int accept(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        GamemodeHandler.acceptDuel(context.getSource().getPlayerOrException(), player);
        return 1;
    }
}
