package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SprintFixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("sprintfix").executes(SprintFixCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SavedPlayerData data = PlayerDataManager.get(player).savedData;

        PlayerUtil.getNexusPlayer(player).sendMessage(ChatFormat.nexiaMessage.append(Component.text((data.setSprintFix(!data.isSprintFix()) ? "Enabled" : "Disabled") + " Sprint Fix!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        return 1;
    }
}
