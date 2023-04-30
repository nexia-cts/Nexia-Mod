package com.nexia.core.commands.player.oitc;

import com.mojang.brigadier.CommandDispatcher;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OitcCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("oitc")
                .requires(commandSourceStack -> {
                    try {
                        return PlayerDataManager.get(commandSourceStack.getPlayerOrException()).gameMode == PlayerGameMode.OITC;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(Commands.literal("join").executes(context -> {
                    OitcGame.joinQueue(context.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("leave").executes(context -> {
                    LobbyUtil.sendGame(context.getSource().getPlayerOrException(), "oitc", false, true);
                    return 1;
                }))
        );
    }
}
