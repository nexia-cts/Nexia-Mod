package com.nexia.core.utilities.player;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.Main;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;

public class BanHandler {

    static String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/tempbans";
    static String playerDataDirectory = dataDirectory + "/playerdata";

    public static int parseTimeArg(String durationArg) throws Exception {
        StringReader stringReader = new StringReader(durationArg);
        float number = stringReader.readFloat();
        String unit = stringReader.readUnquotedString();

        int unitValue = units.getOrDefault(unit, 0);
        int time = Math.round(number * unitValue);
        if (time <= 0) {
            throw new Exception();
        }
        return time;
    }

    public static void addBanToList(GameProfile profile, String reason, int duration) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("uuid", profile.getId().toString());
            jsonObject.put("reason", reason);
            jsonObject.put("duration", duration + System.currentTimeMillis());

            String json = jsonObject.toJSONString();

            File file = new File(new File(dataDirectory), profile.getId().toString() + ".json");
            if(file.createNewFile()){
                FileWriter fileWriter = new FileWriter(dataDirectory + "/" +  profile.getId().toString() + ".json");
                fileWriter.write(json);
                fileWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean removeBanFromList(GameProfile profile){
        File file = new File(new File(dataDirectory), profile.getId().toString() + ".json");
        if(file.exists()) {
            file.delete();
            return true;
        } else { return false; }

    }

    static HashMap<String, Integer> units = new HashMap<>();
    static {
        units.put("s", 1);
        units.put("m", 60);
        units.put("h", 3600);
    }

    public static JSONObject getBanList(String uuid) {
        JSONParser parser = new JSONParser();
        try {
            return (JSONObject) parser.parse(new FileReader(new File(new File(dataDirectory), uuid + ".json")));
        } catch(Exception ignored) { return null; }
    }

    public static void tryBan(CommandSourceStack sender, Collection<GameProfile> collection, int duration, String reason) {

        GameProfile profile = collection.stream().findFirst().get();

        JSONObject BanJSON = getBanList(profile.getId().toString());

        if (BanJSON != null) {
            if((long) BanJSON.get("duration") - System.currentTimeMillis() > 0) {
                removeBanFromList(profile);
            } else {
                sender.sendSuccess(LegacyChatFormat.format("{s}This player has already been banned for {f}{}{s}." +
                        "\n{s}Reason: {f}{}", banTimeToText((long) BanJSON.get("duration") - System.currentTimeMillis()), BanJSON.get("reason")), false);
                return;
            }
        }


        addBanToList(profile, reason, duration);

        sender.sendSuccess(LegacyChatFormat.format("{s}Temp banned {b2}{} {s}for {b2}{}{s}." +
                "\n{s}Reason: {b2}{}", profile.getName(), banTimeToText(duration), reason), false);

        ServerPlayer banned = ServerTime.minecraftServer.getPlayerList().getPlayer(profile.getId());

        if (banned != null) {
            banned.connection.disconnect(new TextComponent("§c§lYou have been banned.\n§7Duration: §d" + banTimeToText(duration) + "\n§7Reason: §d" + reason + "\n§7You can appeal your ban at §d" + Main.config.discordLink));
        }
    }

    public static String banTimeToText(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        long hours = minutes / 60;
        minutes %= 60;

        return hours + "h, " + minutes + "m, " + seconds + "s";
    }

    public static void tryUnBan(CommandSourceStack sender, Collection<GameProfile> collection) {
        GameProfile unBanned = collection.stream().findFirst().get();

        if (!removeBanFromList(unBanned)) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player is not banned."), false);
            return;
        }

        sender.sendSuccess(LegacyChatFormat.format("{s}Unbanned {b2}{}{s}.", unBanned.getName()), false);
    }
}
