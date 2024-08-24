package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.util.Location;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import static net.minecraft.server.commands.LocateBiomeCommand.ERROR_INVALID_BIOME;

public class MapCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register((CommandUtils.literal("map")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.map", 2))
                .then(CommandUtils.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"delete", "create", "tp"}), builder)))
                        .then(CommandUtils.argument("map", ResourceLocationArgument.id())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((WorldUtil.getAllWorldsId()), builder)))
                                .executes((context -> run(context, StringArgumentType.getString(context, "type"), context.getArgument("map", ResourceLocation.class), Biomes.THE_VOID)))
                                .then(CommandUtils.argument("biome", ResourceLocationArgument.id())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggestResource((ServerTime.minecraftServer.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet()), builder)))
                                        .executes((context -> run(context, StringArgumentType.getString(context, "type"), context.getArgument("map", ResourceLocation.class), context.getArgument("biome", ResourceLocation.class))))
                                )
                        )
                )
        ));
    }

    private static int run(CommandContext<CommandSourceInfo> context, String type, ResourceLocation map, ResourceLocation biomeLocation) throws CommandSyntaxException {
        // check if biome is valid
        ServerTime.minecraftServer.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(biomeLocation).orElseThrow(() -> ERROR_INVALID_BIOME.create(biomeLocation));

        return run(context, type, map, ResourceKey.create(Registry.BIOME_REGISTRY, biomeLocation));
    }

    private static int run(CommandContext<CommandSourceInfo> context, String type, ResourceLocation map, @NotNull ResourceKey<Biome> biome) {
        NexiaPlayer player = null;
        if(context.getSource().getExecutingEntity() instanceof Player nexusPlayer) {
            player = new NexiaPlayer(nexusPlayer);
        }

        if(map == null || map.toString().trim().isEmpty() || type.trim().isEmpty()) {
            context.getSource().sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("Invalid name!", ChatFormat.failColor))
            );
            return 1;
        }

        if(type.equalsIgnoreCase("create")){

            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, map).location(), (WorldUtil.defaultWorldConfig.setGenerator(WorldUtil.getChunkGenerator(biome)))).asWorld();

            if (player != null) {
                player.teleport(new Location(0, 80, 0, 0, 0, WorldUtil.getWorld(level)));

                player.sendNexiaMessage(
                        Component.text("Created map called: ", ChatFormat.normalColor)
                                .append(Component.text(map.toString(), ChatFormat.brandColor2))
                );
            }


            return 1;
        }

        if (type.equalsIgnoreCase("delete")) {
            ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, map).location(), null).delete();
            if(player != null) {
                player.sendNexiaMessage(
                        Component.text("Deleted map called: ", ChatFormat.normalColor)
                                .append(Component.text(map.toString(), ChatFormat.brandColor2))
                );
            }

            try {
                FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + map.getNamespace(), map.getPath()));
            } catch (Exception ignored) { }
            return 1;
        }

        if(type.equalsIgnoreCase("tp")) {
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, map).location(), null).asWorld();

            if(player != null) {
                player.teleport(new Location(0, 80, 0, 0, 0, WorldUtil.getWorld(level)));

                player.sendNexiaMessage(
                        Component.text("Teleported to map called: ", ChatFormat.normalColor)
                                .append(Component.text(map.toString(), ChatFormat.brandColor2))
                );
            }

            return 1;
        }
        return 1;
    }
}