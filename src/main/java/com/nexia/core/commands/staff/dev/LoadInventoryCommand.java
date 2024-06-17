package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class LoadInventoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("loadinventory")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.inventory.load", 4);
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .then(Commands.argument("type", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Objects.requireNonNull(new File(InventoryUtil.dirpath).list())), builder)))
                                .then(Commands.argument("inventory", StringArgumentType.string())
                                        .suggests(((context, builder) -> {
                                            ArrayList<String> inventoryList = new ArrayList<>();
                                            try {
                                                inventoryList = InventoryUtil.getListOfInventories(StringArgumentType.getString(context, "type"));
                                            } catch (Exception ignored) {
                                                inventoryList.add("Unable to get inventories!");
                                            }
                                            return SharedSuggestionProvider.suggest(inventoryList, builder);
                                        }))
                                        .executes(context -> run(context, StringArgumentType.getString(context, "type"), StringArgumentType.getString(context, "inventory"), context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> run(context, StringArgumentType.getString(context, "type"), StringArgumentType.getString(context, "inventory"), EntityArgument.getPlayer(context, "player")))
                                        )
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context, @NotNull String type, @NotNull String inventory, @NotNull ServerPlayer player) {
        if(inventory.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty inventory name!"));
            return 0;
        }

        if(type.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty type!"));
            return 0;
        }

        boolean isContextPlayer;
        try {
            isContextPlayer = context.getSource().getPlayerOrException().equals(player);
        } catch (CommandSyntaxException ignored) {
            isContextPlayer = false;
        }

        String gearString = InventoryUtil.getGearStringFromFile(type, inventory);
        if(gearString.isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Unable to load the content of the inventory with the name '{}' in '{}'.", inventory, type));
            return 0;
        } else {
            InventoryUtil.loadInventory(new NexiaPlayer(player), type, inventory);

            context.getSource().sendSuccess(LegacyChatFormat.format("{b1}Successfully loaded '{}' in '{}' to " + ((isContextPlayer) ? "your inventory" : "the inventory of " + player.getScoreboardName()) + ".", inventory, type), false);

            if(!isContextPlayer){
                player.sendMessage(LegacyChatFormat.format("{b1}Your inventory has been replaced with '{}'.", inventory), Util.NIL_UUID);
            }
        }

        return 1;
    }
}
