package com.nexia.core.commands.player.duels.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective_fabric.functions.PlayerFunctions;
import com.natamus.collective_fabric.functions.StringFunctions;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.CustomKitRoom;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.SmpKitRoom;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.VanillaKitRoom;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.players.AccuratePlayer;
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("kiteditor")
                .requires(commandSourceStack -> {
                    try {
                        com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        PlayerData playerData1 = PlayerDataManager.get(commandSourceStack.getPlayerOrException());
                        return playerData.gameMode == DuelGameMode.LOBBY && playerData1.gameMode == PlayerGameMode.LOBBY;
                    } catch (Exception ignored) {}
                    return false;
                })
                .then(Commands.argument("argument", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"save", "edit", "delete"}), builder)))
                                .executes(context -> run(context, StringArgumentType.getString(context, "argument"), ""))
                                .then(Commands.argument("slot", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(slots, builder)))
                                        .executes(context -> run(context, StringArgumentType.getString(context, "argument"), StringArgumentType.getString(context, "slot")))
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context, @NotNull String argument, String slot) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!slots.contains(slot) && !argument.equalsIgnoreCase("save")) {
            context.getSource().sendFailure(LegacyChatFormat.format("Invalid slot!"));
            return 0;
        }



        String inventory = StringUtils.isNumeric(slot) ? "custom_" + slot : slot;

        if(argument.equalsIgnoreCase("save")) {

            com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);

            if(playerData.editingKit.isEmpty() || playerData.kitRoom == null) {
                context.getSource().sendFailure(LegacyChatFormat.format("You aren't editing a kit!"));
                return 0;
            }

            inventory = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).editingKit;

            String gearstring = PlayerFunctions.getPlayerGearString(player);

            Path playerPath = Path.of(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID());
            File playerDir = playerPath.toFile();
            try {
                if(!playerDir.exists()) Files.createDirectory(playerPath);
            } catch (IOException ignored) { }

            KitRoom kitRoom = playerData.kitRoom;

            if (StringFunctions.sequenceCount(gearstring, "\n") < 40 || !playerDir.exists()) {
                context.getSource().sendFailure(LegacyChatFormat.format("Something went wrong while generating the save file content for your inventory."));

                kitRoom.leave();
                ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                return 0;
            } else if (!InventoryUtil.writeGearStringToFile("duels/custom/" + player.getStringUUID(), inventory, gearstring)) {
                context.getSource().sendFailure(LegacyChatFormat.format("Something went wrong while saving the content of your inventory as '{}'.", inventory));

                kitRoom.leave();
                ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                return 0;
            } else {
                context.getSource().sendSuccess(LegacyChatFormat.format("{b1}Successfully saved your inventory as '{}'.", inventory),false);

                kitRoom.leave();
                ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                return 1;
            }
        }

        if (argument.equalsIgnoreCase("edit")) {

            if(!com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).editingKit.isEmpty()) {
                context.getSource().sendFailure(LegacyChatFormat.format("You are still editing a kit! Save it or run /hub!"));
                return 0;
            }

            KitRoom kitRoom;
            com.nexia.minigames.games.duels.util.player.PlayerData playerData = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);


            switch (slot) {
                case "1", "2", "3" -> kitRoom = new CustomKitRoom(accuratePlayer);
                case "vanilla" -> kitRoom = new VanillaKitRoom(accuratePlayer);
                case "smp" -> kitRoom = new SmpKitRoom(accuratePlayer);
                default -> {
                    player.sendMessage(LegacyChatFormat.formatFail("Something went wrong whilst creating your kit room."), Util.NIL_UUID);
                    ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
                    return 0;
                }
            }

            playerData.editingKit = inventory;
            playerData.kitRoom = kitRoom;

            if(kitRoom.generate()) kitRoom.teleport();

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID(), inventory + ".txt");
            if(playerFile.exists()) {
                InventoryUtil.loadInventory(player, "duels/custom/" + player.getStringUUID(), inventory.toLowerCase());
            }

            return 1;
        }

        if(argument.equalsIgnoreCase("delete")) {

            File playerFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + player.getStringUUID(), inventory + ".txt");
            if(playerFile.exists() && playerFile.delete()) {
                context.getSource().sendSuccess(LegacyChatFormat.format("{b1}Successfully deleted slot '{}'!", slot), false);
            } else {
                context.getSource().sendFailure(LegacyChatFormat.format("The saved kit for that slot ({}) does not exist!", slot));
            }

            return 1;
        }

        context.getSource().sendFailure(LegacyChatFormat.format("Invalid argument!"));
        return 1;
    }
}
