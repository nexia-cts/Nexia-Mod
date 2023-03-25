package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class UnMuteCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("unmute")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.mute", 1))

                .then(Commands.argument("player", EntityArgument.player())
                        .executes(UnMuteCommand::unMute)
                )
        );
    }

    public static int unMute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack sender = context.getSource();
        ServerPlayer muted = EntityArgument.getPlayer(context, "player");

        PlayerMutes.unMute(sender, muted);

        return 1;
    }

}
