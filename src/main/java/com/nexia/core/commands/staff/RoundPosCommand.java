package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RoundPosCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("roundpos").executes(RoundPosCommand::run)
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.dev.roundpos", 3)));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        EntityPos pos = new EntityPos(player);
        pos.x = Math.floor(pos.x) + 0.5;
        pos.z = Math.floor(pos.z) + 0.5;
        pos.yaw = Math.round(pos.yaw / 45) * 45;
        pos.pitch = 0;

        pos.teleportPlayer(player.getLevel(), player);
        return 1;
    }

}
