package com.nexia.core.commands.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SprintFixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("sprintfix").executes(SprintFixCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = PlayerUtil.getFactoryPlayer(context.getSource().getPlayerOrException());

        if (player.hasTag("sprint_fix_disable")) {
            player.removeTag("sprint_fix_disable");
        } else {
            player.addTag("sprint_fix_disable");
        }

        return 1;
    }
}
