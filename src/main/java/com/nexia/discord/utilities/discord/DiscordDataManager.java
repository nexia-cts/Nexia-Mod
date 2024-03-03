package com.nexia.discord.utilities.discord;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class DiscordDataManager {

    static String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/discord";
    static String DiscordDataDirectory = dataDirectory + "/discorddata";

    static HashMap<Long, DiscordData> allDiscordData = new HashMap<>();

    public static DiscordData get(long discordID) {
        if (!allDiscordData.containsKey(discordID)) {
            addDiscordData(discordID);
        }
        return allDiscordData.get(discordID);
    }

    public static void addDiscordData(long discordID) {
        DiscordData DiscordData = new DiscordData(loadDiscordData(discordID));
        allDiscordData.put(discordID, DiscordData);
        saveDiscordData(discordID);
    }

    public static void removeDiscordData(long discordID) {
        if (!allDiscordData.containsKey(discordID)) return;
        saveDiscordData(discordID);
        allDiscordData.remove(discordID);
    }

    private static void saveDiscordData(long discordID) {
        try {

            DiscordData DiscordData = get(discordID);
            Gson gson = new Gson();
            String json = gson.toJson(DiscordData.savedData);

            String directory = getDataDir();
            FileWriter fileWriter = new FileWriter(directory + "/" +  discordID + ".json");
            fileWriter.write(json);
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SavedDiscordData loadDiscordData(long discordID) {
        try {

            String directory = getDataDir();
            String json = Files.readString(Path.of(directory + "/" + discordID + ".json"));

            Gson gson = new Gson();
            return gson.fromJson(json, SavedDiscordData.class);

        } catch (Exception e) {
            return new SavedDiscordData();
        }
    }

    private static String getDataDir() {
        new File(DiscordDataDirectory).mkdirs();
        return DiscordDataDirectory;
    }
}
