

package com.nexia.ffa.sky.utilities.player;

import com.google.gson.Gson;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.nexia.ffa.sky.utilities.FfaSkyUtil.killRewards;

public class PlayerDataManager {
    static String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/ffa/sky";
    static String playerDataDirectory;
    static HashMap<UUID, PlayerData> allPlayerData;

    public PlayerDataManager() {
    }

    public static PlayerData get(Player player) {
        if (!allPlayerData.containsKey(player.getUUID())) {
            addPlayerData(player);
        }

        return allPlayerData.get(player.getUUID());
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
