package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective_fabric.functions.PlayerFunctions;
import com.natamus.collective_fabric.functions.StringFunctions;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class SaveInventoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("saveinventory")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.inventory.save", 4)  || FabricLoader.getInstance().isDevelopmentEnvironment();
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .then(Commands.argument("type", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Objects.requireNonNull(new File(InventoryUtil.dirpath).list())), builder)))
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(context -> run(context, StringArgumentType.getString(context, "type"), StringArgumentType.getString(context, "name")))
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context, @NotNull String type, @NotNull String inventory) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if(inventory.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty inventory name!"));
            return 0;
        }

        if(type.trim().isEmpty()) {
            context.getSource().sendFailure(LegacyChatFormat.format("Empty type!"));
            return 0;
        }

        String gearstring = PlayerFunctions.getPlayerGearString(player);
        if (StringFunctions.sequenceCount(gearstring, "\n") < 40) {
            context.getSource().sendFailure(LegacyChatFormat.format("Something went wrong while generating the save file content for your inventory."));
            return 0;
        } else if (!InventoryUtil.writeGearStringToFile(type, inventory, gearstring)) {
            context.getSource().sendFailure(LegacyChatFormat.format("Something went wrong while saving the content of your inventory as '{}' in '{}'.", inventory, type));
            return 0;
        } else {
            context.getSource().sendFailure(LegacyChatFormat.format("Successfully saved your inventory as '{}' in '{}'.", inventory, type));
            context.getSource().sendFailure(LegacyChatFormat.format("{b1}You can load it with the command '/loadinventory {} {}'.", type, inventory));
            return 1;
        }
    }
}
