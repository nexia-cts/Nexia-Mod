package com.nexia.core.commands.player.ffa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.ffa.SpawnGUI;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

public class BiomeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("biome")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if(!FfaClassicUtil.canGoToSpawn(player)) {
                        PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You must be fully healed to change biomes!").color(ChatFormat.failColor));
                        return 1;
                    }
                    LobbyUtil.sendGame(player, "classic ffa", false, true);
                    run(context);
                    return 1;
                })
                .then(Commands.argument("biome", StringArgumentType.greedyString())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(SpawnGUI.mapLocations.keySet(), builder)))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if(!FfaClassicUtil.canGoToSpawn(player)) {
                                PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You must be fully healed to change biomes!").color(ChatFormat.failColor));
                                return 1;
                            }
                            LobbyUtil.sendGame(player, "classic ffa", false, true);
                            selectedMap(context);
                            return 1;
                        })
                )
        );
        dispatcher.register(Commands.literal("spawn")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if(!FfaClassicUtil.canGoToSpawn(player)) {
                        PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You must be fully healed to change biomes!").color(ChatFormat.failColor));
                        return 1;
                    }
                    LobbyUtil.sendGame(player, "classic ffa", false, true);
                    run(context);
                    return 1;
                })
                .then(Commands.argument("biome", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(SpawnGUI.mapLocations.keySet(), builder)))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if(!FfaClassicUtil.canGoToSpawn(player)) {
                                PlayerUtil.getFactoryPlayer(player).sendMessage(Component.text("You must be fully healed to change biomes!").color(ChatFormat.failColor));
                                return 1;
                            }
                            LobbyUtil.sendGame(player, "classic ffa", false, true);
                            selectedMap(context);
                            return 1;
                        })
                )
        );
    }

    public static void run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        SpawnGUI.openSpawnGUI(executer);
    }

    public static void selectedMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SpawnGUI.teleportPlayer(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "biome"));
    }
}