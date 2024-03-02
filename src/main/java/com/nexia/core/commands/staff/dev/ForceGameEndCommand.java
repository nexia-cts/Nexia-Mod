package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

public class ForceGameEndCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("forcegameend")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.dev.forcegameend");
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .then(Commands.argument("game", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"football", "oitc", "skywars", "bedwars"}), builder)))
                                .executes(ForceGameEndCommand::run))
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        String argument = StringArgumentType.getString(context, "game");


        switch (argument) {
            case "football" -> FootballGame.endGame(null);
            case "oitc" -> OitcGame.endGame(null);
            case "skywars" -> SkywarsGame.winNearestCenter();
            case "bedwars" -> BwGame.endBedwars();
            default -> context.getSource().sendFailure(new TextComponent("Invalid game!"));
        }

        context.getSource().sendSuccess(new TextComponent("Game ended (if valid)."), false);
        return 1;
    }
}