package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SwSkipQueueCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("swskipqueue")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.swskipqueue", 1)))
                .executes(SwSkipQueueCommand::run)
        );

    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (SkywarsGame.isStarted || SkywarsGame.queueTime <= 2) {
            player.sendMessage(new TextComponent("Epic queue skip failure"), Util.NIL_UUID);
        } else {
            SkywarsGame.queueTime = 2;
            player.sendMessage(new TextComponent("Skipped queue"), Util.NIL_UUID);
        }

        return 1;
    }
}
