package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class HealCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("heal").executes(HealCommand::run)
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.heal", 1))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> HealCommand.heal(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), false))))
                )
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());
        executor.setHealth(executor.getMaxHealth());

        executor.sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been healed.", ChatFormat.normalColor))
        );

        return 1;
    }

    public static int heal(CommandContext<CommandSourceInfo> context, ServerPlayer otherPlayer) {
        otherPlayer.heal(otherPlayer.getMaxHealth());


       context.getSource().sendMessage(
               ChatFormat.nexiaMessage
                       .append(Component.text("You have healed ", ChatFormat.normalColor))
                       .append(Component.text(otherPlayer.getScoreboardName(), ChatFormat.brandColor2))
                       .append(Component.text(".", ChatFormat.normalColor))
       );


        new NexiaPlayer(otherPlayer).sendNexiaMessage("You have been healed.");
        return 1;
    }
}
