package com.nexia.core.commands.player.ffa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.ffa.KitGUI;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.kits.FfaKit;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

public class KitCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("kit")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if(!FfaKitsUtil.canGoToSpawn(player)) {
                        PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You must be fully healed to change kits!").color(ChatFormat.failColor));
                        return 1;
                    }
                    LobbyUtil.sendGame(player, "kits ffa", false, true);
                    run(context);
                    return 1;
                })
                .then(Commands.argument("inventory", StringArgumentType.greedyString())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(FfaKit.stringFfaKits, builder)))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if(!FfaKitsUtil.canGoToSpawn(player)) {
                                PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You must be fully healed to change kits!").color(ChatFormat.failColor));
                                return 1;
                            }

                            LobbyUtil.sendGame(player, "kits ffa", false, true);
                            selectedMap(context);
                            return 1;
                        })
                )
        );
    }

    public static void run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        KitGUI.openKitGUI(context.getSource().getPlayerOrException());
    }

    public static void selectedMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        KitGUI.giveKit(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "inventory"));
    }
}