package com.nexia.core.commands.player;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;

public class SprintFixCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("sprintfix").executes(SprintFixCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = CommandUtil.getPlayer(context);
        SavedPlayerData data = PlayerDataManager.get(player).savedData;

        player.sendMessage(ChatFormat.nexiaMessage.append(Component.text((data.setSprintFix(!data.isSprintFix()) ? "Enabled" : "Disabled") + " Sprint Fix!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        return 1;
    }
}
