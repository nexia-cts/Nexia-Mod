package com.nexia.core.commands.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.http.DiscordWebhook;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.Main;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
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
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("report")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> ReportCommand.report(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "reason")))
                        )
                )
        );
    }


    public static int report(CommandContext<CommandSourceStack> context, ServerPlayer player, String reason) throws CommandSyntaxException {
        ServerPlayer mcExecutor = context.getSource().getPlayerOrException();
        Player executor = PlayerUtil.getFactoryPlayer(mcExecutor);

        if(PlayerDataManager.get(mcExecutor).savedData.isReportBanned) {
            executor.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You are report banned!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false)
                            )

            );
            return 1;
        }

        if(mcExecutor.equals(player)){
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
                            .append(Component.text("Failed to send webhook! Don't worry, staff who are currently online will see your report!\nIf there are no staff online, feel free to re-send this report."))
            );
        }

        Component staffReportMessage = ChatFormat.nexiaMessage
                .append(Component.text(executor.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                .append(Component.text(" has reported ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                .append(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                .append(Component.text(" for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false));

        for (ServerPlayer staffPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()){
            if(Permissions.check(staffPlayer, "nexia.staff.report", 1)) PlayerUtil.getFactoryPlayer(staffPlayer).sendMessage(staffReportMessage);
        }

        return 1;
    }
}