

package com.nexia.ffa.kits.utilities.player;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class PlayerDataManager {
    static String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/ffa/kits";
    static String playerDataDirectory;
    static HashMap<UUID, PlayerData> allPlayerData;



    public PlayerDataManager() {
    }

    public static PlayerData get(Player player) {
        if (!allPlayerData.containsKey(player.getUUID())) {
            addPlayerData(player);
        }

        return (PlayerData)allPlayerData.get(player.getUUID());
    }

    public static void addPlayerData(Player player) {
        PlayerData playerData = new PlayerData(loadPlayerData(player));
        allPlayerData.put(player.getUUID(), playerData);
    }

    public static void removePlayerData(Player player) {
        if (allPlayerData.containsKey(player.getUUID())) {
            savePlayerData(player);
            allPlayerData.remove(player.getUUID());
        }
    }

    private static void savePlayerData(Player player) {
        try {
            PlayerData playerData = get(player);
            Gson gson = new Gson();
            String json = gson.toJson(playerData.savedData);
            String directory = getDataDir();
            FileWriter fileWriter = new FileWriter(directory + "/" + player.getUUID() + ".json");
            fileWriter.write(json);
            fileWriter.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private static SavedPlayerData loadPlayerData(Player player) {
        try {
            String directory = getDataDir();
            String json = Files.readString(Path.of(directory + "/" + player.getUUID() + ".json"));
            Gson gson = new Gson();
            return gson.fromJson(json, SavedPlayerData.class);
        } catch (Exception var4) {
            return new SavedPlayerData();
        }
    }

    private static String getDataDir() {
        (new File(playerDataDirectory)).mkdirs();
        return playerDataDirectory;
    }

    static {
        playerDataDirectory = dataDirectory + "/playerdata";
        allPlayerData = new HashMap();
    }
}
