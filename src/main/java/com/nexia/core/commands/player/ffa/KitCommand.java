package com.nexia.core.commands.player.ffa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.ffa.KitGUI;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.kits.FfaKit;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

public class KitCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("kit")
                .executes(context -> {
                    NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());
                    if(!FfaKitsUtil.canGoToSpawn(player)) {
                        player.sendMessage(Component.text("You must be fully healed to change kits!", ChatFormat.failColor));
                        return 0;
                    }
                    LobbyUtil.sendGame(player, "kits ffa", false, true);
                    run(player.unwrap());
                    return 1;
                })
                .then(CommandUtils.argument("inventory", StringArgumentType.greedyString())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(FfaKit.stringFfaKits, builder)))
                        .executes(context -> {
                            NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());
                            if(!FfaKitsUtil.canGoToSpawn(player)) {
                                player.sendMessage(Component.text("You must be fully healed to change kits!").color(ChatFormat.failColor));
                                return 1;
                            }

                            LobbyUtil.sendGame(player, "kits ffa", false, true);
                            selectedMap(player.unwrap(), StringArgumentType.getString(context, "inventory"));
                            return 1;
                        })
                )
        );
    }

    public static void run(ServerPlayer player) throws CommandSyntaxException {
        KitGUI.openKitGUI(player);
    }

    public static void selectedMap(ServerPlayer player, String inventory) {
        KitGUI.giveKit(player, inventory);
    }
}