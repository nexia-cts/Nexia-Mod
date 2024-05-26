package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.misc.CommandUtil;
import com.nexia.core.utilities.player.BanHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.Collection;

public class UnTempBanCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("untempban")
                .requires(commandSourceInfo -> {
                    if(CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                    return Permissions.check(CommandUtil.getPlayer(commandSourceInfo).unwrap(), "nexia.staff.ban", 3);
                })

                .then(CommandUtils.argument("player", GameProfileArgument.gameProfile())
                        .executes(UnTempBanCommand::unBan)
                )
        );
    }

    public static int unBan(CommandContext<CommandSourceInfo> context) {
        CommandSourceInfo sender = context.getSource();

        BanHandler.tryUnBan(sender, context.getArgument("player", Collection.class));

        return 1;
    }

}
