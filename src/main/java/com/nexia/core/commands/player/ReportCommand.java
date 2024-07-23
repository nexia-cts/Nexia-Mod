package com.nexia.core.commands.player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.http.DiscordWebhook;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.Main;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.awt.*;

public class ReportCommand {

    private static boolean sendWebhook(String reporter, String victim, String reason){
        DiscordWebhook webhook = new DiscordWebhook(Main.config.reportWebhook);
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
                                .executes(context -> ReportCommand.report(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "reason")))
                        )
                )
        );
    }


    public static int report(CommandContext<CommandSourceInfo> context, ServerPlayer player, String reason) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        if(((CoreSavedPlayerData)PlayerDataManager.getDataManager(com.nexia.core.Main.CORE_DATA_MANAGER).get(executor).savedData).isReportBanned()) {
            executor.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You are report banned!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false)
                            )

            );
            return 1;
        }

        if(executor.getUUID().equals(player.getUUID())){
            executor.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You cannot report yourself!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false))

            );
            return 1;
        }

        executor.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have reported ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                        .append(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2))
                        .append(Component.text(" for ").color(ChatFormat.normalColor))
                        .append(Component.text(reason).color(ChatFormat.brandColor2))
        );

        if(!sendWebhook(executor.getRawName(), player.getScoreboardName(), reason)) {
            executor.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Failed to send webhook! Don't worry, staff who are currently online will see your report!").decoration(ChatFormat.bold, false).color(ChatFormat.normalColor))
            );
        }

        Component staffReportMessage = ChatFormat.nexiaMessage
                .append(Component.text(executor.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                .append(Component.text(" has reported ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                .append(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                .append(Component.text(" for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false));

        for (ServerPlayer staffPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()){
            if(Permissions.check(staffPlayer, "nexia.staff.report", 1)) ServerTime.nexusServer.getPlayer(staffPlayer.getUUID()).sendMessage(staffReportMessage);
        }

        return 1;
    }
}