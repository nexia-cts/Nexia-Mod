package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class SpectateCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("spectate")
                .requires(commandSourceInfo -> {
                    try {
                        NexiaPlayer player = new NexiaPlayer(commandSourceInfo.getPlayerOrException());

                        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
                        CorePlayerData playerData1 = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
                        return (playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY) || (playerData1.gameMode == PlayerGameMode.FFA);
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                        .executes(SpectateCommand::gameModeSpectate)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> SpectateCommand.spectate(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), true))))
                )
        );
    }

    public static int gameModeSpectate(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(executor)).gameMode != PlayerGameMode.FFA) {
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("This can only be used in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("If you are in duels then you do /spectate <player>.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            return 0;
        }

        if(!executor.hasPermission("nexia.prefix.supporter")) {
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("This feature is only available for").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text("Supporters")
                                    .color(ChatFormat.brandColor1)
                                    .decoration(ChatFormat.bold, true)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor1)))
                                    .clickEvent(ClickEvent.suggestCommand("/ranks")
                                    )
                                    .append(Component.text("!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            )
                    )

            );
        }

        if(LobbyUtil.checkGameModeBan(executor, "ffa")) {
            return 0;
        }

        if(Math.round(executor.getHealth()) < 20) {
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("You must be fully healed to go into spectator!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
            );
            return 0;
        }

        executor.setGameMode(Minecraft.GameMode.SPECTATOR);

        return 1;
    }

    public static int spectate(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer nexiaExecutor = new NexiaPlayer(context.getSource().getPlayerOrException());
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode == PlayerGameMode.LOBBY) {
            GamemodeHandler.spectatePlayer(nexiaExecutor, nexiaPlayer);
            return 1;
        }

        if(!nexiaExecutor.hasPermission("nexia.prefix.supporter")) {
            nexiaExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                            Component.text("This feature is only available for").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                    .append(Component.text("Supporters")
                                            .color(ChatFormat.brandColor1)
                                            .decoration(ChatFormat.bold, true)
                                            .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor1)))
                                            .clickEvent(ClickEvent.suggestCommand("/ranks")
                                            )
                                            .append(Component.text("!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                    )
                    )

            );
        }

        if(LobbyUtil.checkGameModeBan(nexiaExecutor, "ffa")) {
            return 0;
        }

        if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaExecutor)).gameMode != PlayerGameMode.FFA) {
            nexiaExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("This can only be used in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            nexiaExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("If you are in duels then you do /spectate <player>.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            return 0;
        }

        if(!FfaUtil.isFfaPlayer(nexiaPlayer)) {
            nexiaExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("That player is not in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
        }

        // Check if player is in combat (or full health), then put them in spectator.

        if(Math.round(nexiaExecutor.getHealth()) < 20) {
            nexiaExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("You must be fully healed to go into spectator!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
            );
            return 0;
        }

        nexiaExecutor.setGameMode(Minecraft.GameMode.SPECTATOR);
        nexiaExecutor.teleport(nexiaPlayer.getLocation());

        return 1;
    }
}
