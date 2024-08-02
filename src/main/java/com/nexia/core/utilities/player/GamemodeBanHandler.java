package com.nexia.core.utilities.player;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static com.nexia.core.utilities.player.BanHandler.getBanTime;

public class GamemodeBanHandler {

    static final String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/tempbans/gamemodebans";

    public static boolean removeBanFromList(UUID uuid, PlayerGameMode gameMode){
        return new File(dataDirectory + "/" + uuid, gameMode.id + ".json").delete();
    }

    public static JSONObject getBanList(UUID uuid, PlayerGameMode gameMode) {
        JSONParser parser = new JSONParser();

        try {
            return (JSONObject) parser.parse(new FileReader(new File(dataDirectory + "/" + uuid, gameMode.id + ".json")));
        } catch(Exception ignored) { return null; }
    }

    public static PlayerGameMode getBannedGameMode(NexiaPlayer player) {
        for(PlayerGameMode gameMode : PlayerGameMode.playerGameModes) {
            JSONObject banList = getBanList(player.getUUID(), gameMode);
            if(banList == null) continue;
            PlayerGameMode parsedGameMode = PlayerGameMode.identifyGamemode((String) banList.get("gamemode"));
            if(parsedGameMode == null) continue;
            return parsedGameMode;
        }
        return null;
    }

    public static ArrayList<PlayerGameMode> getBannedGameModes(NexiaPlayer player) {
        ArrayList<PlayerGameMode> bannedGameModes = new ArrayList<>();

        for(PlayerGameMode gameMode : PlayerGameMode.playerGameModes) {
            JSONObject banList = getBanList(player.getUUID(), gameMode);
            if(banList == null) continue;
            PlayerGameMode parsedGameMode = PlayerGameMode.identifyGamemode((String) banList.get("gamemode"));
            if(parsedGameMode == null) continue;
            bannedGameModes.add(parsedGameMode);
        }

        return bannedGameModes;
    }

    public static void addBanToList(NexiaPlayer player, PlayerGameMode gameMode, String reason, LocalDateTime duration) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("uuid", player.getUUID().toString());
            jsonObject.put("gamemode", gameMode.id);
            jsonObject.put("reason", reason);
            jsonObject.put("duration", duration.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String json = jsonObject.toJSONString();

            Path path = new File(new File(dataDirectory), String.valueOf(player.getUUID())).toPath();

            if(!Files.exists(path)) Files.createDirectory(path);
            File directory = path.toFile();

            File file = new File(directory, gameMode.id + ".json");
            if(file.createNewFile()){
                FileWriter fileWriter = new FileWriter(dataDirectory + "/" +  player.getUUID() + "/" + gameMode.id + ".json");
                fileWriter.write(json);
                fileWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tryGamemodeBan(CommandSourceInfo sender, NexiaPlayer player, PlayerGameMode gameMode, int duration, String reason) {
        JSONObject banJSON = getBanList(player.getUUID(), gameMode);

        if (banJSON != null) {
            LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
            if(LocalDateTime.now().isAfter(banTime)) {
                removeBanFromList(player.getUUID(), gameMode);
            } else {
                sender.sendMessage(Component.text("This player has already been banned in ", ChatFormat.systemColor)
                        .append(Component.text(gameMode.name, ChatFormat.failColor))
                        .append(Component.text(" for ", ChatFormat.systemColor))
                        .append(Component.text(BanHandler.banTimeToText(banTime), ChatFormat.failColor))
                        .append(Component.text(".", ChatFormat.systemColor))
                );

                sender.sendMessage(Component.text("Reason: ", ChatFormat.systemColor)
                        .append(Component.text((String) banJSON.get("reason"), ChatFormat.failColor))
                );

                return;
            }
        }

        LocalDateTime banTime = LocalDateTime.now().plusSeconds(duration);
        addBanToList(player, gameMode, reason, LocalDateTime.now().plusSeconds(duration));

        if(banJSON != null) {
            sender.sendMessage(Component.text(String.format("Gamemode (%s) banned ", gameMode.name), ChatFormat.systemColor)
                    .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                    .append(Component.text(" for ", ChatFormat.systemColor))
                    .append(Component.text(BanHandler.banTimeToText(banTime), ChatFormat.brandColor2))
                    .append(Component.text(".", ChatFormat.systemColor))
            );

            sender.sendMessage(Component.text("Reason: ", ChatFormat.systemColor)
                    .append(Component.text((String) banJSON.get("reason"), ChatFormat.brandColor2))
            );
        }


        LobbyUtil.returnToLobby(player, true);

        player.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have been gamemode (" + gameMode.name + ") banned for ").decoration(ChatFormat.bold, false))
                        .append(Component.text(BanHandler.banTimeToText(banTime)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                        .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                        .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
        );
    }

    public static void tryUnGamemodeBan(CommandSourceInfo sender, NexiaPlayer player, PlayerGameMode gameMode) {
        if (!removeBanFromList(player.getUUID(), gameMode)) {
            sender.sendMessage(Component.text("This player is not banned.", ChatFormat.failColor));
            return;
        }

        sender.sendMessage(Component.text("Un-gamemode-banned ", ChatFormat.systemColor)
                .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                .append(Component.text(" in ", ChatFormat.systemColor))
                .append(Component.text(gameMode.name, ChatFormat.brandColor2))
                .append(Component.text("."))
        );
    }
}
