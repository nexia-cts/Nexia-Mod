package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.uhc.utilities.FfaAreas;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsMap;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

public class DevExperimentalMapCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("devexperimentalmap")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.dev.experimentalmap");
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .then(Commands.argument("argument", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"cffa", "rluhc", "swmap"}), builder)))
                                .executes(DevExperimentalMapCommand::run))
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String argument = StringArgumentType.getString(context, "argument");
        String name = argument;
        if(name.contains("-")) name = name.split("-")[1];

        if(argument.contains("cffa")){
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ffa", name)).location(), (
                    new RuntimeWorldConfig()
                            .setDimensionType(SkywarsGame.world.dimensionType())
                            .setGenerator(SkywarsGame.world.getChunkSource().getGenerator())
                            .setDifficulty(Difficulty.HARD)
                            .setGameRule(GameRules.RULE_KEEPINVENTORY, true)
                            .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                            .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                            .setGameRule(GameRules.RULE_DAYLIGHT, true)
                            .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, true)
                            .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                            .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                            .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
                            .setGameRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK, true)
                            .setGameRule(GameRules.RULE_DROWNING_DAMAGE, true)
                            .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0))).asWorld();

            player.teleportTo(level, 0, 80, 0, 0, 0);
        } else if(argument.equalsIgnoreCase("rluhc")) {
            FfaAreas.resetMap(true);
            player.sendMessage(LegacyChatFormat.format("Reloaded UHC Map."), Util.NIL_UUID);
        } else if(argument.contains("swmap")) {
            SkywarsMap map = SkywarsMap.identifyMap(name);
            if(map != null) {
                SkywarsGame.map = map;
                player.sendMessage(LegacyChatFormat.format("Map set to " + map.id), Util.NIL_UUID);
            } else {
                player.sendMessage(LegacyChatFormat.format("Invalid map!"), Util.NIL_UUID);
            }

        }

        return 1;
    }
}