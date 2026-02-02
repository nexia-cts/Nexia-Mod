package com.nexia.base.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import com.nexia.discord.utilities.player.DiscordSavedPlayerData;
import com.nexia.ffa.base.player.FFASavedPlayerData;
import com.nexia.ffa.kits.utilities.player.KitFFAPlayerData;
import com.nexia.minigames.games.base.player.WLKSavedPlayerData;
import com.nexia.minigames.games.bedwars.util.player.BedwarsPlayerData;
import com.nexia.minigames.games.bedwars.util.player.BedwarsSavedPlayerData;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.minigames.games.duels.util.player.DuelsSavedPlayerData;
import com.nexia.minigames.games.football.util.player.FootballPlayerData;
import com.nexia.minigames.games.football.util.player.FootballSavedPlayerData;
import com.nexia.minigames.games.oitc.util.player.OITCPlayerData;
import com.nexia.minigames.games.skywars.util.player.SkywarsPlayerData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.bson.Document;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Map<ResourceLocation, PlayerDataManager> dataManagerMap = new HashMap<>();

    private final String collectionName;

    HashMap<UUID, PlayerData> allPlayerData = new HashMap<>();

    public Class<? extends SavedPlayerData> savedPlayerDataClass;

    public Class<? extends PlayerData> playerDataClass;

    public PlayerDataManager(ResourceLocation id, String collectionName, Class<? extends SavedPlayerData> savedPlayerDataClass, Class<? extends PlayerData> playerDataClass) {
        this.collectionName = collectionName;
        this.savedPlayerDataClass = savedPlayerDataClass;
        this.playerDataClass = playerDataClass;
        dataManagerMap.put(id, this);
    }

    public static PlayerDataManager getDataManager(ResourceLocation identifier) {
        return dataManagerMap.get(identifier);
    }
    public static void init() {
        // <-----------  Core --------------->
        new PlayerDataManager(NexiaCore.CORE_DATA_MANAGER, "core", CoreSavedPlayerData.class, CorePlayerData.class);

        // <-----------  Discord --------------->
        new PlayerDataManager(NexiaCore.DISCORD_DATA_MANAGER, "discord", DiscordSavedPlayerData.class, PlayerData.class);

        // <-----------  FFAs --------------->
        new PlayerDataManager(NexiaCore.FFA_CLASSIC_DATA_MANAGER, "ffa_classic", FFASavedPlayerData.class, PlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_KITS_DATA_MANAGER, "ffa_kits", FFASavedPlayerData.class, KitFFAPlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_POT_DATA_MANAGER, "ffa_pot", FFASavedPlayerData.class, PlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_SKY_DATA_MANAGER, "ffa_sky", FFASavedPlayerData.class, PlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_UHC_DATA_MANAGER, "ffa_uhc", FFASavedPlayerData.class, PlayerData.class);

        // <-----------  BedWars --------------->
        new PlayerDataManager(NexiaCore.BEDWARS_DATA_MANAGER, "bedwars", BedwarsSavedPlayerData.class, BedwarsPlayerData.class);

        // <-----------  Duels --------------->
        new PlayerDataManager(NexiaCore.DUELS_DATA_MANAGER, "duels", DuelsSavedPlayerData.class, DuelsPlayerData.class);

        // <-----------  Football --------------->
        new PlayerDataManager(NexiaCore.FOOTBALL_DATA_MANAGER, "football", FootballSavedPlayerData.class, FootballPlayerData.class);

        // <-----------  OITC --------------->
        new PlayerDataManager(NexiaCore.OITC_DATA_MANAGER, "oitc", WLKSavedPlayerData.class, OITCPlayerData.class);

        // <-----------  SkyWars --------------->
        new PlayerDataManager(NexiaCore.SKYWARS_DATA_MANAGER, "skywars", WLKSavedPlayerData.class, SkywarsPlayerData.class);
    }

    public PlayerData get(NexiaPlayer player) {
        return get(player.getUUID());
    }

    public void addPlayerData(NexiaPlayer player) {
        addPlayerData(player.getUUID());
    }

    public void removePlayerData(NexiaPlayer player) {
        removePlayerData(player.getUUID());
    }

    public PlayerData get(UUID uuid) {
        if (!allPlayerData.containsKey(uuid)) {
            addPlayerData(uuid);
        }
        return allPlayerData.get(uuid);
    }

    public void addPlayerData(UUID uuid) {
        PlayerData playerData;
        try {
            playerData = playerDataClass.getConstructor(SavedPlayerData.class).newInstance(loadPlayerData(uuid, savedPlayerDataClass));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        allPlayerData.put(uuid, playerData);
    }

    public void removePlayerData(UUID uuid) {
        if (!allPlayerData.containsKey(uuid)) return;
        savePlayerData(uuid);
        allPlayerData.remove(uuid);
    }

    private void savePlayerData(UUID uuid) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        if (!NexiaCore.mongoManager.isConnected()) {
            savePlayerDataToJson(uuid);
            return;
        }

        Document document = NexiaCore.mongoManager.toDocument(get(uuid).savedData);
        document.append("uuid", uuid.toString());
        document.remove("data");

        UpdateResult isReplaced = NexiaCore.mongoManager.getCollection(collectionName).replaceOne(Filters.eq("uuid", uuid.toString()), document);

        if (isReplaced.getMatchedCount() == 0) {
            NexiaCore.mongoManager.getCollection(collectionName).insertOne(document);
        }
    }

    private void savePlayerDataToJson(UUID uuid) {
        try {
            Path jsonFile = getJsonFilePath(uuid);
            Files.createDirectories(jsonFile.getParent());

            String json = GSON.toJson(get(uuid).savedData);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile.toFile()), StandardCharsets.UTF_8)) {
                writer.write(json);
            }
        } catch (IOException e) {
            NexiaCore.logger.error("Failed to save player data to JSON file for UUID {}: {}", uuid, e.getMessage());
        }
    }

    private Path getJsonFilePath(UUID uuid) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("nexia").resolve("playerdata").resolve(collectionName).resolve(uuid.toString() + ".json");
    }

    private <T extends SavedPlayerData> T loadPlayerData(UUID uuid, Class<T> toLoad) throws InstantiationException, IllegalAccessException {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            try {
                return toLoad.getDeclaredConstructor().newInstance();
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        T savedPlayerData;

        if (NexiaCore.mongoManager.isConnected()) {
            savedPlayerData = NexiaCore.mongoManager.getObject(collectionName, Filters.eq("uuid", uuid.toString()), toLoad);
        } else {
            savedPlayerData = loadPlayerDataFromJson(uuid, toLoad);
        }

        if (savedPlayerData == null) {
            try {
                savedPlayerData = toLoad.getDeclaredConstructor().newInstance();
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return savedPlayerData;
    }

    private <T extends SavedPlayerData> T loadPlayerDataFromJson(UUID uuid, Class<T> toLoad) {
        try {
            Path jsonFile = getJsonFilePath(uuid);

            if (!Files.exists(jsonFile)) {
                return null;
            }

            try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile.toFile()), StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, toLoad);
            }
        } catch (IOException e) {
            NexiaCore.logger.error("Failed to load player data from JSON file for UUID {}: {}", uuid, e.getMessage());
            return null;
        }
    }
}
