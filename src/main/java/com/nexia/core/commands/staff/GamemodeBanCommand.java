package com.nexia.core.commands.staff;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.CommandUtil;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.GamemodeBanHandler;
import com.nexia.core.utilities.player.NexiaPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class GamemodeBanCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("gamemodeban")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((PlayerGameMode.stringPlayerGameModes), builder)))
                                .then(CommandUtils.argument("duration", StringArgumentType.word())
                                        .then(CommandUtils.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> GamemodeBanCommand.ban(context.getSource(), context.getArgument("player", ServerPlayer.class), StringArgumentType.getString(context, "gamemode"), StringArgumentType.getString(context, "reason"), StringArgumentType.getString(context, "duration"))))
                                )
                        )
                ));
    }


    public static int ban(CommandSourceInfo sender, ServerPlayer player, String stringGameMode, String reason, String durationArg) {

        PlayerGameMode gameMode = PlayerGameMode.identifyGamemode(stringGameMode);
        if(gameMode == null) {

            sender.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Invalid gamemode!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
            );

            return 1;
        }

        int durationInSeconds;
        try {
            durationInSeconds = BanHandler.parseTimeArg(durationArg);
        } catch (Exception e) {
            sender.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Invalid duration. Examples: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text("1s / 2m / 3h / 4d / 5w").color(ChatFormat.failColor).decoration(ChatFormat.bold, false))
            );
            return 1;
        }

        GamemodeBanHandler.tryGamemodeBan(sender, new NexiaPlayer(player), gameMode, durationInSeconds, reason);

        return 1;
    }
}
