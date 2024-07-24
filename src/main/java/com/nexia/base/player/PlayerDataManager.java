package com.nexia.base.player;

import com.google.gson.Gson;
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

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    public static Map<ResourceLocation, PlayerDataManager> dataManagerMap = new HashMap<>();

    private final String configDir;

    HashMap<UUID, PlayerData> allPlayerData = new HashMap<>();

    public Class<? extends SavedPlayerData> savedPlayerDataClass;

    public Class<? extends PlayerData> playerDataClass;
    public String getDataDirectory() {
        return FabricLoader.getInstance().getConfigDir().toString() + configDir;
    }
    public PlayerDataManager(ResourceLocation id, String configDir, Class<? extends SavedPlayerData> savedPlayerDataClass, Class<? extends PlayerData> playerDataClass) {
        this.configDir = configDir;
        this.savedPlayerDataClass = savedPlayerDataClass;
        this.playerDataClass = playerDataClass;
        dataManagerMap.put(id, this);
    }

    public static PlayerDataManager getDataManager(ResourceLocation identifier) {
        return dataManagerMap.get(identifier);
    }
    public static void init() {
        // <-----------  Core --------------->
        new PlayerDataManager(NexiaCore.CORE_DATA_MANAGER, "/nexia/core", CoreSavedPlayerData.class, CorePlayerData.class);

        // <-----------  Discord --------------->
        new PlayerDataManager(NexiaCore.DISCORD_DATA_MANAGER, "/nexia/discord", DiscordSavedPlayerData.class, PlayerData.class);

        // <-----------  FFAs --------------->
        new PlayerDataManager(NexiaCore.FFA_CLASSIC_DATA_MANAGER, "/nexia/ffa/classic", FFASavedPlayerData.class, PlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_KITS_DATA_MANAGER, "/nexia/ffa/kits", FFASavedPlayerData.class, KitFFAPlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_SKY_DATA_MANAGER, "/nexia/ffa/sky", FFASavedPlayerData.class, PlayerData.class);
        new PlayerDataManager(NexiaCore.FFA_UHC_DATA_MANAGER, "/nexia/ffa/uhc", FFASavedPlayerData.class, PlayerData.class);

        // <-----------  BedWars --------------->
        new PlayerDataManager(NexiaCore.BEDWARS_DATA_MANAGER, "/nexia/bedwars", BedwarsSavedPlayerData.class, BedwarsPlayerData.class);

        // <-----------  Duels --------------->
        new PlayerDataManager(NexiaCore.DUELS_DATA_MANAGER, "/nexia/duels", DuelsSavedPlayerData.class, DuelsPlayerData.class);

        // <-----------  Football --------------->
        new PlayerDataManager(NexiaCore.FOOTBALL_DATA_MANAGER, "/nexia/football", FootballSavedPlayerData.class, FootballPlayerData.class);

        // <-----------  OITC --------------->
        new PlayerDataManager(NexiaCore.OITC_DATA_MANAGER, "/nexia/oitc", WLKSavedPlayerData.class, OITCPlayerData.class);

        // <-----------  SkyWars --------------->
        new PlayerDataManager(NexiaCore.SKYWARS_DATA_MANAGER, "/nexia/skywars", WLKSavedPlayerData.class, SkywarsPlayerData.class);
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

    private <T extends SavedPlayerData> T loadPlayerData(UUID uuid, Class<T> toLoad) throws InstantiationException, IllegalAccessException {
        try {

            String directory = getDataDir();
            String json = Files.readString(Path.of(directory + "/" + uuid + ".json"));

            Gson gson = new Gson();
            return gson.fromJson(json, toLoad);

        } catch (Exception e) {
            return toLoad.newInstance();
        }
    }

    private String getDataDir() {
        String playerDataDirectory = getDataDirectory() + "/playerdata";
        new File(playerDataDirectory).mkdirs();
        return playerDataDirectory;
    }


}
