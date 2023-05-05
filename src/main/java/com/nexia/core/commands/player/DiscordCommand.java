package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("discord")
                .executes(DiscordCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerUtil.getFactoryPlayer(context.getSource().getPlayerOrException()).sendMessage(ChatFormat.returnAppendedComponent(
                ChatFormat.nexiaMessage(),
                Component.text("Link to discord: ").color(ChatFormat.normalColor),
                Component.text(Main.config.discordLink).color(ChatFormat.brandColor2)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.greenColor)))
                        .clickEvent(ClickEvent.openUrl(Main.config.discordLink))
        ));

        return 1;
    }

}
