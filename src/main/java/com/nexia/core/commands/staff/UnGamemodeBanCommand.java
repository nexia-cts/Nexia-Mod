package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.GamemodeBanHandler;
import com.nexia.base.player.NexiaPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;

public class UnGamemodeBanCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("ungamemodeban")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.ban", 3))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("gamemode", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((PlayerGameMode.stringPlayerGameModes), builder)))
                                .executes(UnGamemodeBanCommand::unBan)
                        )
                )
        );
    }

    public static int unBan(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        CommandSourceInfo sender = context.getSource();

        PlayerGameMode gameMode = PlayerGameMode.identifyGamemode(StringArgumentType.getString(context, "gamemode"));
        if(gameMode == null) {
            sender.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Invalid gamemode!", ChatFormat.normalColor))
            );

            return 1;
        }

        GamemodeBanHandler.tryUnGamemodeBan(sender, new NexiaPlayer(context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), false))), gameMode);

        return 1;
    }

}
