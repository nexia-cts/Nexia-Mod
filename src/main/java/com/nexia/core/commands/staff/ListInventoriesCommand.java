package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.commands.CommandUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;

import java.io.File;
import java.util.Objects;

public class ListInventoriesCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register((CommandUtils.literal("listinventories")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.inventory.list", 4))
                .then(CommandUtils.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Objects.requireNonNull(new File(InventoryUtil.dirpath).list())), builder)))
                        .executes(ListInventoriesCommand::run)
                )
        ));
    }

    private static int run(CommandContext<CommandSourceInfo> context){

        String type = StringArgumentType.getString(context, "type");
        if(type.trim().isEmpty()) {
            context.getSource().sendMessage(Component.text("Empty type!", ChatFormat.failColor));
            return 0;
        }

        context.getSource().sendMessage(Component.text("Saved Inventories: ", ChatFormat.normalColor)
                .append(Component.text(InventoryUtil.getListOfInventoriesString(type), ChatFormat.brandColor2))
                .append(Component.text(".", ChatFormat.normalColor))
        );

        return 1;
    }
}

