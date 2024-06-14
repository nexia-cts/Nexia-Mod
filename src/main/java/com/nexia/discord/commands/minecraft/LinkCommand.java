package com.nexia.discord.commands.minecraft;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.discord.Discord;
import com.nexia.discord.utilities.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LinkCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("link")
                .requires(commandSourceStack -> {
                    try {
                        return !PlayerDataManager.get(commandSourceStack.getPlayerOrException().getUUID()).savedData.isLinked;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .executes(LinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

        int id = RandomUtil.randomInt(1000, 9999);

        if (Discord.idMinecraft.containsKey(id)) {
            id = RandomUtil.randomInt(1000, 9999);
        }

        Discord.idMinecraft.put(id, player.getUUID());

        factoryPlayer.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("Your code is: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                        .append(Component.text(id).color(ChatFormat.brandColor1)
                                .decoration(ChatFormat.bold, true)
                                .hoverEvent(HoverEvent.showText(Component.text("Click me to copy").color(ChatFormat.greenColor)))
                                .clickEvent(ClickEvent.copyToClipboard(String.valueOf(id)))
                        ));

        return Command.SINGLE_SUCCESS;
    }

}