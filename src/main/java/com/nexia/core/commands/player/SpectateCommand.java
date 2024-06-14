package com.nexia.core.commands.player;

import com.combatreforged.factory.api.command.CommandSourceInfo;
import com.combatreforged.factory.api.command.CommandUtils;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
                        if(!CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        NexiaPlayer player = CommandUtil.getPlayer(commandSourceInfo);

                        assert player != null;
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
                        PlayerData playerData1 = PlayerDataManager.get(player);
                        return (playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY) || (playerData1.gameMode == PlayerGameMode.FFA);
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                        .executes(SpectateCommand::gameModeSpectate)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .executes(context -> SpectateCommand.spectate(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource()))))
                )
        );
    }

    public static int gameModeSpectate(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = CommandUtil.getPlayer(context);

        if(PlayerDataManager.get(executor).gameMode != PlayerGameMode.FFA) {
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("This can only be used in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("If you are in duels then you do /spectate <player>.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            return 0;
        }

        if(!Permissions.check(executor.unwrap(), "nexia.prefix.supporter")) {
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

    public static int spectate(CommandContext<CommandSourceInfo> context, ServerPlayer player) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer nexiaExecutor = new NexiaPlayer(CommandUtil.getPlayer(context));
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if(PlayerDataManager.get(nexiaPlayer).gameMode == PlayerGameMode.LOBBY) {
            GamemodeHandler.spectatePlayer(nexiaExecutor, nexiaPlayer);
            return 1;
        }

        if(!Permissions.check(nexiaExecutor.unwrap(), "nexia.prefix.supporter")) {
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

        if(LobbyUtil.checkGameModeBan(factoryExecutor, executor, "ffa")) {
            return 0;
        }

        if(PlayerDataManager.get(executor).gameMode != PlayerGameMode.FFA) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("This can only be used in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("If you are in duels then you do /spectate <player>.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            return 0;
        }

        if(!FfaUtil.isFfaPlayer(player)) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
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
