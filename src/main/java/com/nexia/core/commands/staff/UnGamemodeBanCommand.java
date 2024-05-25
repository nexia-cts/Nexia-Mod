package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.GamemodeBanHandler;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.players.AccuratePlayer;

public class UnGamemodeBanCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("ungamemodeban")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.ban", 3))

                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((PlayerGameMode.stringPlayerGameModes), builder)))
                                .executes(UnGamemodeBanCommand::unBan)
                        )
                )
        );
    }

    public static int unBan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack sender = context.getSource();
        ServerPlayer mcExecutor = null;
        Player executor = null;

        try {
            mcExecutor = sender.getPlayerOrException();
        } catch (Exception ignored){ }

        if(mcExecutor != null) {
            executor = PlayerUtil.getFactoryPlayer(mcExecutor);
        }

        PlayerGameMode gameMode = PlayerGameMode.identifyGamemode(StringArgumentType.getString(context, "gamemode"));
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

        GamemodeBanHandler.tryUnGamemodeBan(sender, new NexiaPlayer(new AccuratePlayer(EntityArgument.getPlayer(context, "player"))), gameMode);

        return 1;
    }

}
