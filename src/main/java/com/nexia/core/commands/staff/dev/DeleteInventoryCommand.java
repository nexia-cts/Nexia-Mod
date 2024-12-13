package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class DeleteInventoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("deleteinventory")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.inventory.delete", 4) || FabricLoader.getInstance().isDevelopmentEnvironment();
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
                                        .executes(context -> run(context, StringArgumentType.getString(context, "type"), StringArgumentType.getString(context, "inventory")))
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context, @NotNull String type, @NotNull String inventory) {
        if(inventory.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty inventory name!"));
            return 0;
        }

        if(type.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty type!"));
            return 0;
        }


        File file = new File(InventoryUtil.dirpath + File.separator + type, inventory + ".txt");

        if(file.isDirectory() || !file.exists()) {
            context.getSource().sendFailure(LegacyChatFormat.format("The inventory with the name '{}' in '{}' does not exist!", inventory, type));
            return 0;
        }

        file.delete();
        context.getSource().sendSuccess(LegacyChatFormat.format("{b1}Successfully deleted inventory '{}' in '{}'.", inventory, type), true);


        return 1;
    }
}
