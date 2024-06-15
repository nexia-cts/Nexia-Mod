package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.BanHandler;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.Collection;

public class TempBanCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("tempban")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", GameProfileArgument.gameProfile())
                        .then(CommandUtils.argument("duration", StringArgumentType.word())
                                .then(CommandUtils.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> TempBanCommand.ban(context.getSource(), context.getArgument("player", GameProfileArgument.Result.class).getNames(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "reason"), StringArgumentType.getString(context, "duration"))))
                )
        ));
    }


    public static int ban(CommandSourceInfo sender, Collection<GameProfile> collection, String reason, String durationArg) {
        int durationInSeconds;
        try {
            durationInSeconds = BanHandler.parseTimeArg(durationArg);
        } catch (Exception e) {
            sender.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Invalid duration. Examples: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                    .append(Component.text("1s / 2m / 3h / 4d / 5w").color(ChatFormat.failColor).decoration(ChatFormat.bold, false))
            );
            return 1;
        }
        
        BanHandler.tryBan(sender, collection, durationInSeconds, reason);
        
        return 1;
    }
}
