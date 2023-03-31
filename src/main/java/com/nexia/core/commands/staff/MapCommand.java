package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelsSpawn;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.stream.Stream;

public class MapCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("map")
                .requires(commandSourceStack -> {
                    try {
                        return PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.map", 2);
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"delete", "create", "tp"}), builder)))
                        .then(Commands.argument("map", StringArgumentType.greedyString())
                                .executes(MapCommand::run)
                        )
                )
        ));
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String type = StringArgumentType.getString(context, "type");
        String map = StringArgumentType.getString(context, "map");

        if((ChatFormat.hasWhiteSpacesOrSpaces(null, map) || ChatFormat.hasWhiteSpacesOrSpaces(null, type)) || (type == null || map == null)) {
            player.sendMessage(ChatFormat.formatFail("Invalid name!"), Util.NIL_UUID);
            return 1;
        }

        String[] mapname = map.split(":");

        if(type.equalsIgnoreCase("create")){

            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), (
                    new RuntimeWorldConfig()
                    .setDimensionType(DuelsSpawn.duelWorld.dimensionType())
                    .setGenerator(DuelsSpawn.duelWorld.getChunkSource().getGenerator())
                    .setDifficulty(Difficulty.HARD)
                    .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                    .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                    .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                    .setGameRule(GameRules.RULE_DAYLIGHT, false)
                    .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                    .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                    .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                    .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0))).asWorld();

            player.teleportTo(level, 0, 80, 0, 0, 0);

            player.sendMessage(ChatFormat.format("{b1}Created map called: {b2}{}{b1}.", mapname[1]), Util.NIL_UUID);
            return 1;
        }

        if (type.equalsIgnoreCase("delete")) {
            ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).delete();
            player.sendMessage(ChatFormat.format("{b1}Deleted map called: {b2}{}{b1}.", mapname[1]), Util.NIL_UUID);
            return 1;
        }

        if(type.equalsIgnoreCase("tp")) {
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).asWorld();
            player.teleportTo(level, 0, 80, 0, 0, 0);

            player.sendMessage(ChatFormat.format("{b1}Teleported to map called: {b2}{}{b1}.", mapname[1]), Util.NIL_UUID);
            return 1;
        }
        return 1;
    }
}
