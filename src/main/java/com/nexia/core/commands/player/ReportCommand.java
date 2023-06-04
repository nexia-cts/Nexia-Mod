package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.http.DiscordWebhook;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportCommand {

    private static void sendWebhook(String reporter, String victim, String reason){
        DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/1076143579585593404/WAvzxt2x1hPOX2UHrklkiCumwVU7xBvUjZ91InBnHKCuuL0nnKPERaEiQxQvgkUxHnZ0");
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setAuthor((reporter + " reported " + victim), null, null)
                .setColor(Color.RED)
                .addField("Reporter:", reporter, true)
                .addField("Reported Player:", victim, true)
                .setThumbnail("https://mc-heads.net/avatar/" + victim + "/64")
                .addField("Reason:", reason, false)
                .setFooter("Nexia • " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()), "https://notcoded.needs.rest/r/nexia.png"));
        try {
            webhook.execute();
        } catch (Exception ignored) { }
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
        ServerPlayer executor = context.getSource().getPlayerOrException();
        if(executor == player){
            player.sendMessage(ChatFormat.formatFail("You cannot report yourself!"), Util.NIL_UUID);
            return 1;
        }

        executor.sendMessage(ChatFormat.format("{b1}You have reported {b2}{} {b1}for {b2}{}.", player.getScoreboardName(), reason), Util.NIL_UUID);
        ServerPlayer staffPlayer;
        sendWebhook(executor.getScoreboardName(), player.getScoreboardName(), reason);
        for (int i = 0; i != Main.server.getPlayerCount(); i++){
            staffPlayer = PlayerUtil.getPlayerFromName(Main.server.getPlayerNames()[i]);
            CommandSourceStack staffStack = staffPlayer.createCommandSourceStack();
            if(PlayerUtil.hasPermission(staffStack, "nexia.staff.report", 1)) {
                staffStack.sendSuccess(ChatFormat.format("{b2}{} {b1}has reported {b2}{} {b1}for {b2}{}.", executor.getScoreboardName(), player.getScoreboardName(), reason), false);
            }
        }

        return 1;
    }
}
