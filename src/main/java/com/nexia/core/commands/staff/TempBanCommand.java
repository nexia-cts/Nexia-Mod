package com.nexia.core.commands.staff;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.Collection;

public class TempBanCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("tempban")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> TempBanCommand.ban(context.getSource(), GameProfileArgument.getGameProfiles(context, "player"), StringArgumentType.getString(context, "reason"), StringArgumentType.getString(context, "duration"))))
                )
        ));
    }


    public static int ban(CommandSourceStack sender, Collection<GameProfile> collection, String reason, String durationArg) {
        int durationInSeconds;
        try {
            durationInSeconds = BanHandler.parseTimeArg(durationArg);
        } catch (Exception e) {
            PlayerUtil.safeSendMessage(sender, LegacyChatFormat.format("{f}Invalid duration. Examples: 1s / 2m / 3h / 4d / 5w"), ChatFormat.nexiaMessage
                    .append(Component.text("Invalid duration. Examples: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                    .append(Component.text("1s / 2m / 3h / 4d / 5w").color(ChatFormat.failColor).decoration(ChatFormat.bold, false)), false);

            return 1;
        }
        
        BanHandler.tryBan(sender, collection, durationInSeconds, reason);
        
        return 1;
    }
}
