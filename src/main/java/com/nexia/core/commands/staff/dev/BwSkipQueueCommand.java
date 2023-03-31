package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class BwSkipQueueCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("bwskipqueue")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.bwskipqueue", 1)))
                .executes(BwSkipQueueCommand::run)
        );

    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (BwUtil.skipQueue()) {
            player.sendMessage(new TextComponent("Skipped queue"), Util.NIL_UUID);
        } else {
            player.sendMessage(new TextComponent("Epic queue skip failure"), Util.NIL_UUID);
        }

        return 1;
    }
}
