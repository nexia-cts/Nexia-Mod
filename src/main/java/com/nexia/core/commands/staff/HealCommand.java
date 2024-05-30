package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class HealCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("heal").executes(HealCommand::run)
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.heal", 1))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> HealCommand.heal(context, context.getArgument("player", ServerPlayer.class)))
                )
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {

        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;

        NexiaPlayer executor = CommandUtil.getPlayer(context);
        executor.setHealth(executor.unwrap().getMaxHealth());

        executor.sendMessage(
                ChatFormat.nexiaMessage
                                .append(Component.text("You have been healed.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
        );

        return 1;
    }

    public static int heal(CommandContext<CommandSourceInfo> context, ServerPlayer otherPlayer) {
        otherPlayer.heal(otherPlayer.getMaxHealth());


       context.getSource().sendMessage(
               ChatFormat.nexiaMessage
                       .append(Component.text("You have healed ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                       .append(Component.text(otherPlayer.getScoreboardName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                       .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
       );


        new NexiaPlayer(otherPlayer).sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have been healed.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
        );

        return 1;
    }
}
