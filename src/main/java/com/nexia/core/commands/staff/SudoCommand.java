package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class SudoCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {

        dispatcher.register(CommandUtils.literal("sudo")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.sudo"))
                .then(CommandUtils.argument("player", EntityArgument.player())
                    .then(CommandUtils.argument("command", StringArgumentType.string())
                        .executes(context -> SudoCommand.execute(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), false)), StringArgumentType.getString(context, "command")))))
        );
    }

    private static int execute(CommandContext<CommandSourceInfo> context, ServerPlayer player, String command) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
        nexiaPlayer.runCommand(command, 4, false);

        return 0;
    }
}
