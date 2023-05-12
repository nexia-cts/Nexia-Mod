package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;

public class UnTempBanCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("untempban")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))

                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(UnTempBanCommand::unBan)
                )
        );
    }

    public static int unBan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack sender = context.getSource();

        BanHandler.tryUnBan(sender, GameProfileArgument.getGameProfiles(context, "player"));

        return 1;
    }

}
