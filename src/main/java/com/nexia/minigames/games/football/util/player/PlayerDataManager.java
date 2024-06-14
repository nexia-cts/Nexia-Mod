package com.nexia.minigames.games.football.util.player;

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

    static String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/football";
    static String playerDataDirectory = dataDirectory + "/playerdata";

    static HashMap<UUID, PlayerData> allPlayerData = new HashMap<>();


    public static PlayerData get(NexiaPlayer player) {
        return get(player.getUUID());
    }

    public static void addPlayerData(NexiaPlayer player) {
        addPlayerData(player.getUUID());
    }


    public static void removePlayerData(NexiaPlayer player) {
        removePlayerData(player.getUUID());
    }


    public static PlayerData get(UUID uuid) {
        if (!allPlayerData.containsKey(uuid)) {
            addPlayerData(uuid);
        }
        return allPlayerData.get(player.getUUID());
    }

    public static void addPlayerData(Player player) {
        PlayerData playerData = new PlayerData(loadPlayerData(player));
        allPlayerData.put(player.getUUID(), playerData);
    }

    public static void removePlayerData(Player player) {
        if (!allPlayerData.containsKey(player.getUUID())) return;
        savePlayerData(player);
        allPlayerData.remove(player.getUUID());
    }

    private static void savePlayerData(Player player) {
        try {

            PlayerData playerData = get(player);
            Gson gson = new Gson();
            String json = gson.toJson(playerData.savedData);

            String directory = getDataDir();
            FileWriter fileWriter = new FileWriter(directory + "/" +  player.getUUID() + ".json");
            fileWriter.write(json);
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SavedPlayerData loadPlayerData(Player player) {
        try {

            String directory = getDataDir();
            String json = Files.readString(Path.of(directory + "/" + player.getUUID() + ".json"));

            Gson gson = new Gson();
            return gson.fromJson(json, SavedPlayerData.class);

        } catch (Exception e) {
            return new SavedPlayerData();
        }
    }

    private static String getDataDir() {
        new File(playerDataDirectory).mkdirs();
        return playerDataDirectory;
    }


}
