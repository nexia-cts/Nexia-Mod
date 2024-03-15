package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.misc.RandomUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class RandomCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("random")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.random", 1))
                .then(Commands.argument("minimum", IntegerArgumentType.integer())
                        .then(Commands.argument("maximum", IntegerArgumentType.integer())
                                .executes(context -> RandomCommand.calculate(context, IntegerArgumentType.getInteger(context, "minimum"), IntegerArgumentType.getInteger(context, "maximum")))))
        );
    }

    public static int calculate(CommandContext<CommandSourceStack> context, int minimum, int maximum) {
        int random = RandomUtil.randomInt(minimum, maximum);
        context.getSource().sendSuccess(new TextComponent(String.valueOf(random)), false);
        return random;
    }
}
