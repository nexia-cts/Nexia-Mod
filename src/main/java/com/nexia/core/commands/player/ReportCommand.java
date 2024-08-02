package com.nexia.core.commands.player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.http.DiscordWebhook;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.builder.implementation.world.entity.player.WrappedPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.awt.*;

public class ReportCommand {

    private static boolean sendWebhook(String reporter, String victim, String reason){
        DiscordWebhook webhook = new DiscordWebhook(NexiaDiscord.config.reportWebhook);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setAuthor((reporter + " reported " + victim), null, null)
                .setColor(Color.RED)

                .addField("Server: ", ServerTime.serverType.type.toUpperCase(), false)
                .addField("Reporter:", reporter, true)
                .addField("Reported Player:", victim, true)
                .setThumbnail("https://mc-heads.net/avatar/" + victim + "/64")
                .addField("Reason:", reason, false)
        );
        try {
            webhook.execute();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("report")
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> ReportCommand.report(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true)), StringArgumentType.getString(context, "reason")))
                        )
                )
        );
    }


    public static int report(CommandContext<CommandSourceInfo> context, ServerPlayer player, String reason) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        if(((CoreSavedPlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(executor).savedData).isReportBanned()) {
            executor.sendNexiaMessage("You are report banned!");
            return 1;
        }

        if(executor.getUUID().equals(player.getUUID())){
            executor.sendNexiaMessage("You cannot report yourself!");
            return 1;
        }

        executor.sendNexiaMessage(
                Component.text("You have reported ", ChatFormat.normalColor)
                        .append(Component.text(player.getScoreboardName(), ChatFormat.brandColor2))
                        .append(Component.text(" for ", ChatFormat.normalColor))
                        .append(Component.text(reason, ChatFormat.brandColor2))
        );

        if(!sendWebhook(executor.getRawName(), player.getScoreboardName(), reason)) {
            executor.sendNexiaMessage("Failed to send webhook! Don't worry, staff who are currently online will see your report!");
        }

        Component staffReportMessage = ChatFormat.nexiaMessage
                .append(Component.text(executor.getRawName(), ChatFormat.brandColor2))
                .append(Component.text(" has reported ", ChatFormat.normalColor))
                .append(Component.text(player.getScoreboardName(), ChatFormat.brandColor2))
                .append(Component.text(" for ", ChatFormat.normalColor))
                .append(Component.text(reason, ChatFormat.brandColor2));

        for (Player staffPlayer : ServerTime.nexusServer.getPlayers()){
            if(Permissions.check(((WrappedPlayer) staffPlayer).unwrap(), "nexia.staff.report", 1)) staffPlayer.sendMessage(staffReportMessage);
        }

        return 1;
    }
}