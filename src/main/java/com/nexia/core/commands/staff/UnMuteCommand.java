package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.commands.CommandUtil;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class UnMuteCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("unmute")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.mute", 1))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(UnMuteCommand::unMute)
                )
        );
    }

    public static int unMute(CommandContext<CommandSourceInfo> context) {
        CommandSourceInfo sender = context.getSource();
        ServerPlayer muted = context.getArgument("player", ServerPlayer.class);

        PlayerMutes.unMute(sender, muted);

        return 1;
    }

}
