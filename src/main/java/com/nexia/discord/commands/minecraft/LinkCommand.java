package com.nexia.discord.commands.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.discord.commands.discord.LinkSlashCommand;
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
                        return !PlayerDataManager.getDataManager(NexiaCore.DISCORD_DATA_MANAGER).get(commandSourceInfo.getPlayerOrException().getUUID()).savedData.get(Boolean.class, "isLinked");
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

        LinkSlashCommand.idMinecraft.put(id, player.getUUID());

        player.sendNexiaMessage(
                Component.text("Your code is: ", ChatFormat.normalColor)
                        .append(Component.text(id, ChatFormat.brandColor1)
                                .decoration(ChatFormat.bold, true)
                                .hoverEvent(HoverEvent.showText(Component.text("Click me to copy").color(ChatFormat.greenColor)))
                                .clickEvent(ClickEvent.copyToClipboard(String.valueOf(id)))
                        ));

        return id;
    }

}