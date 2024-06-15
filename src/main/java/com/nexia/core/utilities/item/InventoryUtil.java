package com.nexia.core.utilities.item;

import com.natamus.collective_fabric.functions.PlayerFunctions;
import com.nexia.core.utilities.player.NexiaPlayer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class InventoryUtil {

    public final static String dirpath = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/inventories";

    public static boolean writeGearStringToFile(String addedPath, String filename, String gearString) {
        File dir = new File(dirpath + File.separator + addedPath);
        dir.mkdirs();

        try {
            PrintWriter writer = new PrintWriter(dir + File.separator + filename + ".txt", StandardCharsets.UTF_8);
            writer.println(gearString);
            writer.close();
            return true;
        } catch (Exception var4) {
            return false;
        }
    }

    public static String getGearStringFromFile(String addedPath, String filename) {
        File dir = new File(dirpath + File.separator + addedPath);
        File file = new File(dir + File.separator + filename + ".txt");
        String gearstring = "";
        if (dir.isDirectory() && file.isFile()) {
            try {
                gearstring = new String(Files.readAllBytes(Paths.get(dir + File.separator + filename + ".txt")));
            } catch (IOException ignored) { }
        }

        return gearstring;
    }

    public static String getListOfInventoriesString(String type) {
        StringBuilder inventories = new StringBuilder();
        File folder = new File(dirpath + File.separator + type);
        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null)
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        if (!inventories.toString().trim().isEmpty()) {
                            inventories.append(", ");
                        }

                        inventories.append(listOfFile.getName().replace(".txt", ""));
                    }
                }
        }
        return inventories.toString();
    }

    public static ArrayList<String> getListOfInventories(String type) {

        ArrayList<String> list = new ArrayList<>();
        File folder = new File(dirpath + File.separator + type);
        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null)
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) list.add(listOfFile.getName().replace(".txt", ""));
                }
        }
        return list;
    }

    public static boolean loadInventory(@NotNull NexiaPlayer player, @NotNull String type, @NotNull String inventoryName) {
        if(type.trim().isEmpty() || inventoryName.trim().isEmpty()) return false;

        PlayerFunctions.setPlayerGearFromString(player.unwrap(), getGearStringFromFile(type, inventoryName));
        return true;
    }

}
