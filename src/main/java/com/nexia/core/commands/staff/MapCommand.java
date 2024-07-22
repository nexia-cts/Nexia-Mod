package com.nexia.core.commands.staff;

import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.util.Location;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.io.File;

import static com.nexia.core.utilities.world.WorldUtil.getChunkGenerator;

public class MapCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register((CommandUtils.literal("map")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.map", 2))
                .then(CommandUtils.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"delete", "create", "tp"}), builder)))
                        .then(CommandUtils.argument("map", StringArgumentType.greedyString())
                                .executes(MapCommand::run)
                        )
                )
        ));
    }

    private static int run(CommandContext<CommandSourceInfo> context) {
        NexiaPlayer player = null;
        if(context.getSource().getExecutingEntity() instanceof Player nexusPlayer) {
            player = new NexiaPlayer(nexusPlayer);
        }

        String type = StringArgumentType.getString(context, "type");
        String map = StringArgumentType.getString(context, "map");

        if(map.trim().isEmpty() || type.trim().isEmpty()) {
            context.getSource().sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Invalid name!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false))
            );
            return 1;
        }

        String[] mapname = map.split(":");

        if(type.equalsIgnoreCase("create")){

            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), (
                    new RuntimeWorldConfig()
                            .setDimensionType(DimensionType.OVERWORLD_LOCATION)
                            .setGenerator(getChunkGenerator(Biomes.THE_VOID))
                            .setDifficulty(Difficulty.HARD)
                            .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                            .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                            .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                            .setGameRule(GameRules.RULE_DAYLIGHT, false)
                            .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                            .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                            .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                            .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0))).asWorld();

            if (player != null) {
                player.teleport(new Location(0, 80, 0, 0, 0, WorldUtil.getWorld(level)));

                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Created map called: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(map).color(ChatFormat.brandColor2))
                );
            }


            return 1;
        }

        if (type.equalsIgnoreCase("delete")) {
            ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).delete();
            if(player != null) {
                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Deleted map called: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(map).color(ChatFormat.brandColor2))
                );
            }

            try {
                FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + mapname[0], mapname[1]));
            } catch (Exception ignored) { }
            return 1;
        }

        if(type.equalsIgnoreCase("tp")) {
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).asWorld();

            if(player != null) {
                player.teleport(new Location(0, 80, 0, 0, 0, WorldUtil.getWorld(level)));

                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Teleported to map called: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                .append(Component.text(map).color(ChatFormat.brandColor2))
                );
            }

            return 1;
        }
        return 1;
    }
}