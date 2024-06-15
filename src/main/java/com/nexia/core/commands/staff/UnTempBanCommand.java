package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.BanHandler;
import net.minecraft.commands.arguments.GameProfileArgument;

public class UnTempBanCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("untempban")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", GameProfileArgument.gameProfile())
                        .executes(UnTempBanCommand::unBan)
                )
        );
    }

    public static int unBan(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        CommandSourceInfo sender = context.getSource();

        BanHandler.tryUnBan(sender, context.getArgument("player", GameProfileArgument.Result.class).getNames(CommandUtil.getCommandSourceStack(context.getSource())));

        return 1;
    }

}
