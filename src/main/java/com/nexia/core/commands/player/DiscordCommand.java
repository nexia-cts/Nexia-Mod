package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("discord").executes(DiscordCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) {


        TextComponent message = new TextComponent(ChatFormat.brandColor1 + "Link to discord: ");

        TextComponent discordLink = new TextComponent(ChatFormat.brandColor2 + "\247n" + Main.config.discordLink);
        discordLink.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.config.discordLink)));

        message.append(discordLink);

        CommandSourceStack player = context.getSource();
        player.sendSuccess(message, false);



        return 1;
    }

}
