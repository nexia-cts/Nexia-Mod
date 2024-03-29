package com.nexia.core.utilities.item;

import com.combatreforged.metis.api.world.entity.player.Player;
import com.natamus.collective_fabric.functions.PlayerFunctions;
import com.natamus.collective_fabric.functions.StringFunctions;
import com.nexia.core.utilities.chat.ChatFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InventoryUtil {

    static String dirpath = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/inventories";


    /*
    public static boolean writeGearStringToFile(String filename, String gearstring, @Nullable String path) {
        File dir = new File(dirpath);
        dir.mkdirs();

        try {
            PrintWriter writer = new PrintWriter(dirpath + "/" + filename + ".txt", StandardCharsets.UTF_8);
            if(path != null){
                writer = new PrintWriter(dirpath + path + "/" + filename + ".txt", StandardCharsets.UTF_8);
            }
            writer.println(gearstring);
            writer.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getGearStringFromFile(String filename, @Nullable String path) {
        File dir = new File(dirpath);
        if(path != null){
            dir = new File(dirpath + path);
        }
        File file = new File(dirpath + "/" + filename + ".txt");

        String gearstring = "";
        if (dir.isDirectory() && file.isFile()) {
            try {
                gearstring = new String(Files.readAllBytes(Paths.get(dirpath + "/" + filename + ".txt", new String[0])));
            }
            catch (IOException ignored) { }
        }

        return gearstring;
    }

    public static String getListOfInventories(@Nullable String path) {
        StringBuilder inventories = new StringBuilder();

        File folder = new File(dirpath);
        if(path != null) {
            folder = new File(dirpath + path);
        }
        if (!folder.isDirectory()) {
            return inventories.toString();
        }

        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (!inventories.toString().equals("")) {
                    inventories.append(", ");
                }
                inventories.append(listOfFile.getName().replace(".txt", ""));
            }
        }

        return inventories.toString();
    }
    public static int saveInventory(Player player, String name, @Nullable String path, boolean silent) {
        if (name.trim() == "") {
            if(!silent){
                player.sendMessage(Component.text("The inventory name " + name + " is invalid."), Util.NIL_UUID);
            }
            return 0;
        }

        String gearstring = PlayerFunctions.getPlayerGearString(player);
        if (StringFunctions.sequenceCount(gearstring, "\n") < 40) {
            if(!silent){
                player.sendMessage(Component.text("Something went wrong while generating the save file content for your inventory."), Util.NIL_UUID);
            }
            return 0;
        }

        if (!InventoryUtil.writeGearStringToFile(name, gearstring, path)) {
            player.sendMessage(Component.text("Something went wrong while saving the content of your inventory as '" + name + "'."), Util.NIL_UUID);
            return 0;
        }

        if(!silent){
            player.sendMessage(Component.text("{b1}Successfully saved your inventory as {b2}'{}'{b1}.", name), Util.NIL_UUID);
        }
        return 1;
    }

    public static int setInventory(Player player, String name, @Nullable String path, boolean silent) {
        if (name.trim() == "") {
            if(!silent){
                player.sendMessage(Component.text("The inventory name '" + name + "' is invalid."), Util.NIL_UUID);
            }
            return 0;
        }

        String gearstring = InventoryUtil.getGearStringFromFile(name, path);
        if (gearstring == "") {
            if(!silent){
                player.sendMessage(Component.text("Unable to load the content of the inventory with the name '" + name + "'."));
            }
            return 0;
        }

        PlayerFunctions.setPlayerGearFromString(player, gearstring);
        if(!silent){
            player.sendMessage(Component.text(String.format("{b1}Your inventory has been set to the preset {b2}{}{b1}.", name)
        }
        return 1;
    }

     */

}
