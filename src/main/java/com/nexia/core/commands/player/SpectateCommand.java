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

        if(!executor.isInGameMode(PlayerGameMode.FFA)) {
            executor.sendNexiaMessage("This can only be used in FFA!");
            executor.sendNexiaMessage("If you are in duels then you do /spectate <player>.");
            return 0;
        }

        if(!executor.hasPermission("nexia.prefix.supporter")) {
            executor.sendNexiaMessage(
                    Component.text("This feature is only available for", ChatFormat.normalColor)
                            .append(Component.text("Supporters")
                                    .color(ChatFormat.brandColor1)
                                    .decoration(ChatFormat.bold, true)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor1)))
                                    .clickEvent(ClickEvent.suggestCommand("/ranks"))
                                    .append(Component.text("!", ChatFormat.normalColor))
                            )
            );
        }

        if(LobbyUtil.checkGameModeBan(executor, "ffa")) {
            return 0;
        }

        if(Math.round(executor.getHealth()) < 20) {
            executor.sendNexiaMessage("You must be fully healed to go into spectator!");
            return 0;
        }

        executor.setGameMode(Minecraft.GameMode.SPECTATOR);

        return 1;
    }

    public static int spectate(CommandContext<CommandSourceInfo> context, ServerPlayer player) throws CommandSyntaxException {
        NexiaPlayer nexiaExecutor = new NexiaPlayer(context.getSource().getPlayerOrException());
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if(nexiaPlayer.isInGameMode(PlayerGameMode.LOBBY)) {
            GamemodeHandler.spectatePlayer(nexiaExecutor, nexiaPlayer);
            return 1;
        }

        if(!nexiaExecutor.hasPermission("nexia.prefix.supporter")) {
            nexiaExecutor.sendNexiaMessage(
                            Component.text("This feature is only available for", ChatFormat.normalColor)
                                    .append(Component.text("Supporters")
                                            .color(ChatFormat.brandColor1)
                                            .decoration(ChatFormat.bold, true)
                                            .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor1)))
                                            .clickEvent(ClickEvent.suggestCommand("/ranks")
                                            )
                                            .append(Component.text("!", ChatFormat.normalColor))
                                    )
            );
        }

        if(LobbyUtil.checkGameModeBan(nexiaExecutor, "ffa")) {
            return 0;
        }

        if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaExecutor)).gameMode != PlayerGameMode.FFA) {
            nexiaExecutor.sendNexiaMessage("This can only be used in FFA!");
            nexiaExecutor.sendNexiaMessage("If you are in duels then you do /spectate <player>.");
        }

        if(!FfaUtil.isFfaPlayer(nexiaPlayer)) {
            nexiaExecutor.sendNexiaMessage("That player is not in FFA!");
            return 0;
        }

        // Check if player is in combat (or full health), then put them in spectator.

        if(Math.round(nexiaExecutor.getHealth()) < 20) {
            nexiaExecutor.sendNexiaMessage("You must be fully healed to go into spectator!");
            return 0;
        }

        nexiaExecutor.setGameMode(Minecraft.GameMode.SPECTATOR);
        nexiaExecutor.teleport(nexiaPlayer.getLocation());

        return 1;
    }
}
