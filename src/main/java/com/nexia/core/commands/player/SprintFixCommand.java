package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.SavedPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;

public class SprintFixCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("sprintfix").executes(SprintFixCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());
        SavedPlayerData data = PlayerDataManager.get(player).savedData;

        player.sendMessage(ChatFormat.nexiaMessage.append(Component.text((data.setSprintFix(!data.isSprintFix()) ? "Enabled" : "Disabled") + " Sprint Fix!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        return 1;
    }
}
