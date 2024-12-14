package com.nexia.core.commands.player.duels.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective_fabric.functions.PlayerFunctions;
import com.natamus.collective_fabric.functions.StringFunctions;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.CustomKitRoom;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.SmpKitRoom;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.VanillaKitRoom;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class KitEditorCommand {

    private static final List<String> slots = Arrays.asList("1", "2", "3", "smp", "vanilla");

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register((CommandUtils.literal("kiteditor")
                .requires(commandSourceInfo -> {
                    try {
                        NexiaPlayer player = new NexiaPlayer(commandSourceInfo.getPlayerOrException());

                        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
                        CorePlayerData playerData1 = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(CommandUtils.argument("argument", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"save", "edit", "delete"}), builder)))
                                .executes(context -> run(context, StringArgumentType.getString(context, "argument"), ""))
                                .then(CommandUtils.argument("slot", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(slots, builder)))
                                        .executes(context -> run(context, StringArgumentType.getString(context, "argument"), StringArgumentType.getString(context, "slot")))
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceInfo> context, @NotNull String argument, String slot) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());

        if (!slots.contains(slot) && !argument.equalsIgnoreCase("save")) {
            player.sendMessage(Component.text("Invalid slot!", ChatFormat.failColor));
            return 0;
        }



        String inventory = StringUtils.isNumeric(slot) ? "custom_" + slot : slot;

        if(argument.equalsIgnoreCase("save")) {

            DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

            if(playerData.editingKit.isEmpty() || playerData.kitRoom == null) {
                player.sendMessage(Component.text("You aren't editing a kit!", ChatFormat.failColor));
                return 0;
            }

            inventory = ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).editingKit;

            String gearstring = PlayerFunctions.getPlayerGearString(player.unwrap());

            Path playerPath = Path.of(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getUUID());
            File playerDir = playerPath.toFile();
            try {
                if(!playerDir.exists()) Files.createDirectory(playerPath);
            } catch (IOException e) {
                if(NexiaCore.config.debugMode) NexiaCore.logger.info("[DEBUG]: Failed to create custom duels inventory path for {}!\nPath: {}\n{}", player, playerPath.toString(), e.getMessage());
            }

            KitRoom kitRoom = playerData.kitRoom;

            if (StringFunctions.sequenceCount(gearstring, "\n") < 40 || !playerDir.exists()) {
                player.sendMessage(Component.text("Something went wrong while generating the save file content for your inventory.", ChatFormat.failColor));
                kitRoom.leave();
                player.runCommand("/hub", 0, false);
                return 0;
            } else if (!InventoryUtil.writeGearStringToFile("duels/custom/" + player.getUUID(), inventory, gearstring)) {
                player.sendMessage(Component.text(String.format("Something went wrong while saving the content of your inventory as '%s'.", inventory), ChatFormat.failColor));

                kitRoom.leave();
                player.runCommand("/hub", 0, false);
                return 0;
            } else {
                player.sendMessage(Component.text(String.format("Successfully saved your inventory as '%s'.", inventory), ChatFormat.failColor));

                kitRoom.leave();
                player.runCommand("/hub", 0, false);
                return 1;
            }
        }

        if (argument.equalsIgnoreCase("edit")) {

            if(!((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).editingKit.isEmpty()) {
                player.sendMessage(Component.text("You are still editing a kit! Save it or run /hub!", ChatFormat.failColor));
                return 0;
            }

            KitRoom kitRoom;
            DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);


            switch (slot) {
                case "1", "2", "3" -> kitRoom = new CustomKitRoom(player);
                case "vanilla" -> kitRoom = new VanillaKitRoom(player);
                case "smp" -> kitRoom = new SmpKitRoom(player);
                default -> {
                    player.sendMessage(Component.text("Something went wrong whilst creating your kit room.", ChatFormat.failColor));
                    player.runCommand("/hub", 0, false);
                    return 0;
                }
            }

            playerData.editingKit = inventory;
            playerData.kitRoom = kitRoom;

            if(kitRoom.generate()) kitRoom.teleport();

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getUUID(), inventory + ".txt");
            if(playerFile.exists()) {
                InventoryUtil.loadInventory(player, "duels/custom/" + player.getUUID(), inventory.toLowerCase());
            }

            return 1;
        }

        if(argument.equalsIgnoreCase("delete")) {

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getUUID(), inventory + ".txt");
            if(playerFile.exists() && playerFile.delete()) {
                player.sendMessage(Component.text(String.format("Successfully deleted slot '%s’!", slot), ChatFormat.brandColor1));
            } else {
                player.sendMessage(Component.text(String.format("The saved kit for that slot (%s) does not exist!", slot), ChatFormat.failColor));
            }

            return 1;
        }

        player.sendMessage(Component.text("Invalid argument!", ChatFormat.failColor));
        return 1;
    }
}
