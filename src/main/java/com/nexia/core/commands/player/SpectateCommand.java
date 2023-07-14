package com.nexia.core.commands.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class SpectateCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("spectate")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return (playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY) || (playerData1.gameMode == PlayerGameMode.FFA);
                    } catch (Exception ignored) {
                    }
                    return false;
                })
                        .executes(SpectateCommand::gameModeSpectate)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> SpectateCommand.spectate(context, EntityArgument.getPlayer(context, "player")))
                )
        );
    }

    public static int gameModeSpectate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor);

        if(PlayerDataManager.get(executor).gameMode != PlayerGameMode.FFA) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("This can only be used in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("If you are in duels then you do /spectate <player>.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
            return 0;
        }

        if(!Permissions.check(executor, "nexia.prefix.supporter")) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
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

        // Check if player is in combat (or full health), then put them in spectator.

        if(factoryExecutor.getHealth() < 20) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("You must be fully healed to go into spectator!").decoration(ChatFormat.bold, false))
            );
            return 0;
        }

        factoryExecutor.setGameMode(Minecraft.GameMode.SPECTATOR);

        return 1;
    }

    public static int spectate(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor);

        if(PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY) {
            GamemodeHandler.spectatePlayer(executor, player);
            return 1;
        }

        if(!Permissions.check(executor, "nexia.prefix.supporter")) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
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

        if(!FfaUtil.isFfaPlayer(player)) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("That player is not in FFA!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
            ));
        }

        // Check if player is in combat (or full health), then put them in spectator.

        if(factoryExecutor.getHealth() < 20) {
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("You must be fully healed to go into spectator!").decoration(ChatFormat.bold, false))
            );
            return 0;
        }

        factoryExecutor.setGameMode(Minecraft.GameMode.SPECTATOR);
        executor.teleportTo(FfaAreas.ffaWorld, player.getX(), player.getY(), player.getZ(), 0, 0);

        return 1;
    }
}
