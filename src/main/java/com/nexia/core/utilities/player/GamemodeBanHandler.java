package com.nexia.core.utilities.player;

import com.nexia.core.games.util.PlayerGameMode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class GamemodeBanHandler extends BanHandler {

    static final String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/tempbans/gamemodebans";

    public static boolean removeBanFromList(ServerPlayer player, PlayerGameMode gameMode){
        return new File(dataDirectory + "/" +  player.getStringUUID(), gameMode.id + ".json").delete();
    }

    public static JSONObject getBanList(String uuid, PlayerGameMode gameMode) {
        JSONParser parser = new JSONParser();
        try {
            return (JSONObject) parser.parse(new FileReader(new File(dataDirectory + "/" +  uuid, gameMode.id + ".json")));
        } catch(Exception ignored) { return null; }
    }

    public static void addBanToList(ServerPlayer player, PlayerGameMode gameMode, String reason, int duration) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("uuid", player.getUUID());
            jsonObject.put("gamemode", gameMode.id);
            jsonObject.put("reason", reason);
            jsonObject.put("duration", duration + System.currentTimeMillis());

            String json = jsonObject.toJSONString();


            File directory = new File(new File(dataDirectory), player.getStringUUID());
            directory.createNewFile();

            File file = new File(directory, gameMode.id + ".json");
            if(file.createNewFile()){
                FileWriter fileWriter = new FileWriter(dataDirectory + "/" +  player.getStringUUID() + "/" + gameMode.id + ".json");
                fileWriter.write(json);
                fileWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
