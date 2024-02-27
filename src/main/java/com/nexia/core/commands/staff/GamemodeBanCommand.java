package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class GamemodeBanCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("gamemodeban")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("gamemode", StringArgumentType.greedyString())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((PlayerGameMode.stringPlayerGameModes), builder)))
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> GamemodeBanCommand.ban(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "gamemode"), StringArgumentType.getString(context, "reason"), StringArgumentType.getString(context, "duration"))))
                                )
                        )
                ));
    }


    public static int ban(CommandSourceStack sender, ServerPlayer player, String stringGameMode, String reason, String durationArg) {
        ServerPlayer mcExecutor = null;
        Player executor = null;

        try {
            mcExecutor = sender.getPlayerOrException();
        } catch (Exception ignored){ }

        if(mcExecutor != null) {
            executor = PlayerUtil.getFactoryPlayer(mcExecutor);
        }

        PlayerGameMode gameMode = PlayerGameMode.identifyGamemode(stringGameMode);
        if(gameMode == null) {

            if(executor != null) {
                executor.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Invalid gamemode!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                );
            } else {
                sender.sendFailure(LegacyChatFormat.format("{f}Invalid gamemode!"));
            }

            return 1;
        }

        int durationInSeconds;
        try {
            durationInSeconds = BanHandler.parseTimeArg(durationArg);
        } catch (Exception e) {
            if (executor != null) {
                executor.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Invalid duration. Examples: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text("1s / 2m / 3h").color(ChatFormat.failColor).decoration(ChatFormat.bold, false))
                );
            } else {
                sender.sendFailure(LegacyChatFormat.format("{f}Invalid duration. Examples: 1s / 2m / 3h"));
            }
            return 1;
        }

        BanHandler.tryGamemodeBan(sender, player, gameMode, durationInSeconds * 1000, reason);

        return 1;
    }
}
