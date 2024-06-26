package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayList;

public class ListInventoriesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("listinventories")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.inventory.list", 4);
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> {
                            ArrayList<String> inventoryList = new ArrayList<>();
                            try {
                                inventoryList = InventoryUtil.getListOfInventories(StringArgumentType.getString(context, "type"));
                            } catch (Exception ignored) {
                                inventoryList.add("Unable to get inventories!");
                            }
                            return SharedSuggestionProvider.suggest(inventoryList, builder);
                        }))
                        .executes(ListInventoriesCommand::run)
                )
        ));
    }

    private static int run(CommandContext<CommandSourceStack> context){

        String type = StringArgumentType.getString(context, "type");
        if(type.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty type!"));
            return 0;
        }

        context.getSource().sendSuccess(LegacyChatFormat.format("{b1}Saved inventories: {b2}{}{b1}.", InventoryUtil.getListOfInventoriesString(type)), false);

        return 1;
    }
}

