package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
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


        PlayerUtil.getFactoryPlayer(executer).sendMessage(ChatFormat.returnAppendedComponent(
                ChatFormat.nexiaMessage(),
                Component.text("You have been healed.").color(ChatFormat.normalColor)
        ));

        return 1;
    }

    public static int heal(CommandContext<CommandSourceStack> context, ServerPlayer otherPlayer) {
        CommandSourceStack executer = context.getSource();
        otherPlayer.heal(otherPlayer.getMaxHealth());

        ServerPlayer player = null;
        try {
            player = executer.getPlayerOrException();

        } catch(Exception ignored) { }


        if(player != null){
            PlayerUtil.getFactoryPlayer(player).sendMessage(ChatFormat.returnAppendedComponent(
                    ChatFormat.nexiaMessage(),
                    Component.text("You have healed ").color(ChatFormat.normalColor),
                    Component.text(otherPlayer.getScoreboardName()).color(ChatFormat.brandColor2),
                    Component.text(".").color(ChatFormat.normalColor)
            ));
        } else {
            executer.sendSuccess(LegacyChatFormat.format("{b1}You have healed {b2}{}{b1}.", otherPlayer.getScoreboardName()), false);
        }


        PlayerUtil.getFactoryPlayer(otherPlayer).sendMessage(ChatFormat.returnAppendedComponent(
                ChatFormat.nexiaMessage(),
                Component.text("You have been healed.").color(ChatFormat.normalColor)
        ));

        return 1;
    }
}
