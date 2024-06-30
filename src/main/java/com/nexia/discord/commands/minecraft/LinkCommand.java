package com.nexia.discord.commands.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.discord.Discord;
import com.nexia.discord.utilities.player.PlayerDataManager;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class LinkCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("link")
                .requires(commandSourceInfo -> {
                    try {
                        return !PlayerDataManager.get(commandSourceInfo.getPlayerOrException().getUUID()).savedData.isLinked;
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                .executes(LinkCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        int id = RandomUtil.randomInt(1000, 9999);

        if (Discord.idMinecraft.containsKey(id)) {
            id = RandomUtil.randomInt(1000, 9999);
        }

        Discord.idMinecraft.put(id, player.getUUID());

        player.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("Your code is: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                        .append(Component.text(id).color(ChatFormat.brandColor1)
                                .decoration(ChatFormat.bold, true)
                                .hoverEvent(HoverEvent.showText(Component.text("Click me to copy").color(ChatFormat.greenColor)))
                                .clickEvent(ClickEvent.copyToClipboard(String.valueOf(id)))
                        ));

        return id;
    }

}