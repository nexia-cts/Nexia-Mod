package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.classic.utilities.FfaAreas;
import net.kyori.adventure.text.Component;
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
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.io.File;

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
        ServerPlayer mcPlayer = context.getSource().getPlayerOrException();
        Player player = PlayerUtil.getFactoryPlayer(mcPlayer);

        String type = StringArgumentType.getString(context, "type");
        String map = StringArgumentType.getString(context, "map");

        if(ChatFormat.hasWhiteSpacesOrSpaces(map) || ChatFormat.hasWhiteSpacesOrSpaces(type)) {
            player.sendMessage(
                    ChatFormat.nexiaMessage
                                    .append(Component.text("Invalid name!").color(ChatFormat.failColor).decoration(ChatFormat.bold, false))
            );

            return 1;
        }

        String[] mapname = map.split(":");

        if(type.equalsIgnoreCase("create")){

            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), (
                    new RuntimeWorldConfig()
                    .setDimensionType(FfaAreas.ffaWorld.dimensionType())
                    .setGenerator(FfaAreas.ffaWorld.getChunkSource().getGenerator())
                    .setDifficulty(Difficulty.HARD)
                    .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                    .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                    .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                    .setGameRule(GameRules.RULE_DAYLIGHT, false)
                    .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                    .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                    .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                    .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0))).asWorld();

            mcPlayer.teleportTo(level, 0, 80, 0, 0, 0);

            player.sendMessage(
                    ChatFormat.nexiaMessage
                                    .append(Component.text("Created map called: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                            .append(Component.text(map).color(ChatFormat.brandColor2))
            );

            return 1;
        }

        if (type.equalsIgnoreCase("delete")) {
            ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).delete();
            player.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Deleted map called: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(map).color(ChatFormat.brandColor2))
            );
            try {
                FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + mapname[0], mapname[1]));
            } catch (Exception ignored) { }
            return 1;
        }

        if(type.equalsIgnoreCase("tp")) {
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).asWorld();
            mcPlayer.teleportTo(level, 0, 80, 0, 0, 0);

            player.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Teleported to map called: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(map).color(ChatFormat.brandColor2))
            );
            return 1;
        }
        return 1;
    }
}
