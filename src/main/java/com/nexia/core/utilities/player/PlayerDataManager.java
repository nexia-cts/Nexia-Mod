package com.nexia.core.utilities.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class PlayerDataManager {

    static String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/core";
    static String playerDataDirectory = dataDirectory + "/playerdata";

    static HashMap<UUID, PlayerData> allPlayerData = new HashMap<>();

    public static PlayerData get(net.minecraft.world.entity.player.Player player) {
        return get(player.getUUID());
    }

    public static PlayerData get(Player player) {
        return get(player.getUUID());
    }

    public static void addPlayerData(net.minecraft.world.entity.player.Player player) {
        addPlayerData(player.getUUID());
    }

    public static void addPlayerData(Player player) {
        addPlayerData(player.getUUID());
    }

    public static void removePlayerData(net.minecraft.world.entity.player.Player player) {
        removePlayerData(player.getUUID());
    }

    public static void removePlayerData(Player player) {
        removePlayerData(player.getUUID());
    }

    public static PlayerData get(UUID uuid) {
        if (!allPlayerData.containsKey(uuid)) {
            addPlayerData(uuid);
        }
        return allPlayerData.get(uuid);
    }

    public static void addPlayerData(UUID uuid) {
        PlayerData playerData = new PlayerData(loadPlayerData(uuid));
        allPlayerData.put(uuid, playerData);
    }

    public static void removePlayerData(UUID uuid) {
        if (!allPlayerData.containsKey(uuid)) return;
        savePlayerData(uuid);
        allPlayerData.remove(uuid);
    }

    private static void savePlayerData(UUID uuid) {
        try {

            PlayerData playerData = get(uuid);
            Gson gson = new Gson();
            String json = gson.toJson(playerData.savedData);

            String directory = getDataDir();
            FileWriter fileWriter = new FileWriter(directory + "/" +  uuid + ".json");
            fileWriter.write(json);
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SavedPlayerData loadPlayerData(UUID uuid) {
        try {

            String directory = getDataDir();
            String json = Files.readString(Path.of(directory + "/" + uuid + ".json"));

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
