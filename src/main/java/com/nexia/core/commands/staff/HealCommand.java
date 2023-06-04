package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class HealCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("heal").executes(HealCommand::run)
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.heal", 1))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> HealCommand.heal(context, EntityArgument.getPlayer(context, "player")))
                )
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        executer.heal(executer.getMaxHealth());

        executer.sendMessage(ChatFormat.format("{b1}You have been healed."), Util.NIL_UUID);

        return 1;
    }

    public static int heal(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        CommandSourceStack executer = context.getSource();
        player.heal(player.getMaxHealth());

        executer.sendSuccess(ChatFormat.format("{b1}You have healed {b2}{}{b1}.", player.getScoreboardName()), false);
        player.sendMessage(ChatFormat.format("{b1}You have been healed."), Util.NIL_UUID);

        return 1;
    }
}
