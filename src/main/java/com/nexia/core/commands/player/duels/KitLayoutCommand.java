package com.nexia.core.commands.player.duels;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KitLayoutCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register((CommandUtils.literal("kitlayout")
                .requires(commandSourceInfo -> {
                    try {
                        if(!CommandUtil.checkPlayerInCommand(commandSourceInfo)) return false;
                        NexiaPlayer player = CommandUtil.getPlayer(commandSourceInfo);

                        assert player != null;
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
                        PlayerData playerData1 = PlayerDataManager.get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(CommandUtils.argument("argument", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"save", "edit", "reset"}), builder)))
                                .executes(context -> run(context, StringArgumentType.getString(context, "argument"), ""))
                                .then(CommandUtils.argument("inventory", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(InventoryUtil.getListOfInventories("duels"), builder)))
                                        .executes(context -> run(context, StringArgumentType.getString(context, "argument"), StringArgumentType.getString(context, "inventory")))
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceInfo> context, @NotNull String argument, @NotNull String inventory) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer player = new NexiaPlayer(CommandUtil.getPlayer(context));

        if((inventory.trim().isEmpty() || !InventoryUtil.getListOfInventories("duels").contains(inventory.toLowerCase())) && !argument.equalsIgnoreCase("save")) {
            player.sendMessage(Component.text("Invalid gamemode!", ChatFormat.failColor));
            return 0;
        }

        if(argument.equalsIgnoreCase("save")) {

            if(com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).editingLayout.isEmpty()) {
                player.sendMessage(Component.text("You aren't editing a layout!", ChatFormat.failColor));
                return 0;
            }

            inventory = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).editingLayout;

            String path = InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getUUID() + File.separator + "layout";
            Path playerPath = Path.of(path);

            File playerDir = playerPath.toFile();
            try {
                if(!playerDir.exists()) Files.createDirectory(playerPath);
            } catch (IOException ignored) { }

            SavableInventory savableInventory = new SavableInventory(player.unwrap().inventory);
            String stringInventory = savableInventory.toSave();

            try {
                String file = path + File.separator + inventory + ".json";
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(stringInventory);
                fileWriter.close();
            } catch (Exception var6) {
                player.runCommand("/hub", 0, false);
                player.sendMessage(Component.text(String.format("Failed to save your inventory as '%s'. Please try again or contact a developer.", inventory), ChatFormat.failColor));
                return 0;
            }

            player.sendMessage(Component.text(String.format("Successfully saved your inventory as '%s'.", inventory), ChatFormat.brandColor1));
            player.runCommand("/hub", 0, false);
            return 1;
        }

        if (argument.equalsIgnoreCase("edit")) {

            if(!com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).editingLayout.isEmpty()) {
                player.sendMessage(Component.text("You are still editing a layuot! Save it run or run /hub!", ChatFormat.failColor));
                return 0;
            }

            DuelGameHandler.loadInventory(player, inventory);

            com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).editingLayout = inventory;

            return 1;
        }

        if(argument.equalsIgnoreCase("reset")) {

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getUUID() + File.separator + "layout" + File.separator + inventory + ".json");
            if(playerFile.exists() && playerFile.delete()) {
                player.sendMessage(Component.text(String.format("Successfully reset saved layout '%s'!", inventory), ChatFormat.brandColor1));
            } else {
                player.sendMessage(Component.text("Saved Layout does not exist!", ChatFormat.failColor));
            }

            return 1;
        }

        player.sendMessage(Component.text("Invalid argument!", ChatFormat.failColor));
        return 1;
    }
}
