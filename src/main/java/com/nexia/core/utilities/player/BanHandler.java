package com.nexia.core.utilities.player;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;

public class BanHandler {

    static final String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/tempbans";

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

    public static void addBanToList(GameProfile profile, String reason, LocalDateTime duration) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("uuid", profile.getId().toString());
            jsonObject.put("reason", reason);
            jsonObject.put("duration", duration.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

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
        return new File(new File(dataDirectory), profile.getId().toString() + ".json").delete();
    }

    static HashMap<String, Integer> units = new HashMap<>();
    static {
        units.put("s", 1);
        units.put("m", 60);
        units.put("h", 3600);
        units.put("d", 86400);
        units.put("w", 604800);
    }

    public static JSONObject getBanList(String uuid) {
        JSONParser parser = new JSONParser();
        try {
            return (JSONObject) parser.parse(new FileReader(new File(new File(dataDirectory), uuid + ".json")));
        } catch(Exception ignored) { return null; }
    }

    public static void tryBan(CommandSourceInfo sender, Collection<GameProfile> collection, int duration, String reason) {

        GameProfile profile = collection.stream().findFirst().get();

        JSONObject banJSON = getBanList(profile.getId().toString());

        if (banJSON != null) {
            LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
            if(LocalDateTime.now().isAfter(banTime)) {
                removeBanFromList(profile);
            } else {
                sender.sendMessage(Component.text("This player has already been banned for ", ChatFormat.systemColor)
                        .append(Component.text(banTimeToText(banTime), ChatFormat.failColor))
                        .append(Component.text(".", ChatFormat.systemColor))
                );

                sender.sendMessage(Component.text("Reason: ", ChatFormat.systemColor)
                        .append(Component.text((String) banJSON.get("reason"), ChatFormat.failColor))
                );

                return;
            }
        }

        LocalDateTime banTime = LocalDateTime.now().plusSeconds(duration);
        addBanToList(profile, reason, LocalDateTime.now().plusSeconds(duration));

        sender.sendMessage(Component.text("Temp banned ", ChatFormat.systemColor)
                .append(Component.text(profile.getName(), ChatFormat.brandColor2))
                .append(Component.text(" for ", ChatFormat.systemColor))
                .append(Component.text(banTimeToText(banTime), ChatFormat.brandColor2))
                .append(Component.text(".", ChatFormat.systemColor))
        );

        sender.sendMessage(Component.text("Reason: ", ChatFormat.systemColor)
                .append(Component.text(reason, ChatFormat.brandColor2))
        );

        ServerPlayer banned = ServerTime.minecraftServer.getPlayerList().getPlayer(profile.getId());

        if (banned != null) {
            banned.connection.disconnect(new TextComponent("§c§lYou have been banned.\n§7Duration: §d" + banTimeToText(banTime) + "\n§7Reason: §d" + reason + "\n§7You can appeal your ban at §d" + NexiaDiscord.config.discordLink));
        }
    }


    public static LocalDateTime getBanTime(String duration) {
        LocalDateTime banTime;
        try {
            banTime = LocalDateTime.parse(duration, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            banTime = LocalDateTime.MIN;
        }
        return banTime;
    }

    public static String banTimeToText(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(now, localDateTime);
        try {
            return DurationFormatUtils.formatDuration(duration.toMillis(), "d'd', HH'h', mm'm', ss's'", true);
        } catch (Exception ignored) {
            return "Invalid time!";
        }

    }

    public static void tryUnBan(CommandSourceInfo sender, Collection<GameProfile> collection) {
        GameProfile unBanned = collection.stream().findFirst().get();

        if (!removeBanFromList(unBanned)) {
            sender.sendMessage(Component.text("This player is not banned.", ChatFormat.failColor));
            return;
        }

        sender.sendMessage(Component.text("Unbanned ", ChatFormat.systemColor)
                .append(Component.text(unBanned.getName(), ChatFormat.brandColor2))
                .append(Component.text(".", ChatFormat.systemColor))
        );
    }
}
