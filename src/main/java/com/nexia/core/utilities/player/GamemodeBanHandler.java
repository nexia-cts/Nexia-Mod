package com.nexia.core.utilities.player;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
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

import static com.nexia.core.utilities.player.BanHandler.getBanTime;

public class GamemodeBanHandler {

    static final String dataDirectory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/tempbans/gamemodebans";

    public static boolean removeBanFromList(String uuid, PlayerGameMode gameMode){
        return new File(dataDirectory + "/" + uuid, gameMode.id + ".json").delete();
    }

    public static JSONObject getBanList(String uuid, PlayerGameMode gameMode) {
        JSONParser parser = new JSONParser();

        try {
            return (JSONObject) parser.parse(new FileReader(new File(dataDirectory + "/" + uuid, gameMode.id + ".json")));
        } catch(Exception ignored) { return null; }
    }

    public static PlayerGameMode getBannedGameMode(ServerPlayer player) {
        for(PlayerGameMode gameMode : PlayerGameMode.playerGameModes) {
            JSONObject banList = getBanList(player.getStringUUID(), gameMode);
            if(banList == null) continue;
            PlayerGameMode parsedGameMode = PlayerGameMode.identifyGamemode((String) banList.get("gamemode"));
            if(parsedGameMode == null) continue;
            return parsedGameMode;
        }
        return null;
    }

    public static ArrayList<PlayerGameMode> getBannedGameModes(ServerPlayer player) {
        ArrayList<PlayerGameMode> bannedGameModes = new ArrayList<>();

        for(PlayerGameMode gameMode : PlayerGameMode.playerGameModes) {
            JSONObject banList = getBanList(player.getStringUUID(), gameMode);
            if(banList == null) continue;
            PlayerGameMode parsedGameMode = PlayerGameMode.identifyGamemode((String) banList.get("gamemode"));
            if(parsedGameMode == null) continue;
            bannedGameModes.add(parsedGameMode);
        }

        return bannedGameModes;
    }

    public static void addBanToList(ServerPlayer player, PlayerGameMode gameMode, String reason, LocalDateTime duration) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("uuid", player.getStringUUID());
            jsonObject.put("gamemode", gameMode.id);
            jsonObject.put("reason", reason);
            jsonObject.put("duration", duration.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String json = jsonObject.toJSONString();

            Path path = new File(new File(dataDirectory), player.getStringUUID()).toPath();

            if(!Files.exists(path)) Files.createDirectory(path);
            File directory = path.toFile();

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

    public static void tryGamemodeBan(CommandSourceStack sender, ServerPlayer player, PlayerGameMode gameMode, int duration, String reason) {
        JSONObject banJSON = getBanList(player.getStringUUID(), gameMode);

        if (banJSON != null) {
            LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
            if(LocalDateTime.now().isAfter(banTime)) {
                removeBanFromList(player.getStringUUID(), gameMode);
            } else {
                sender.sendSuccess(LegacyChatFormat.format("{s}This player has already been banned in {f}{} for {f}{}{s}." +
                        "\n{s}Reason: {f}{}", gameMode.name, BanHandler.banTimeToText(banTime), banJSON.get("reason")), false);
                return;
            }
        }

        LocalDateTime banTime = LocalDateTime.now().plusSeconds(duration);
        addBanToList(player, gameMode, reason, LocalDateTime.now().plusSeconds(duration));

        sender.sendSuccess(LegacyChatFormat.format("{s}Gamemode ({}) banned {b2}{} {s}for {b2}{}{s}." +
                "\n{s}Reason: {b2}{}", gameMode.name, player.getScoreboardName(), BanHandler.banTimeToText(banTime), reason), false);

        ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");

        PlayerUtil.getFactoryPlayer(player).sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have been gamemode (" + gameMode.name + ") banned for ").decoration(ChatFormat.bold, false))
                        .append(Component.text(BanHandler.banTimeToText(banTime)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                        .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                        .append(Component.text(reason).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
        );
    }

    public static void tryUnGamemodeBan(CommandSourceStack sender, ServerPlayer player, PlayerGameMode gameMode) {
        if (!removeBanFromList(player.getStringUUID(), gameMode)) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player is not banned."), false);
            return;
        }

        sender.sendSuccess(LegacyChatFormat.format("{s}Un-gamemode-banned {b2}{}{s} in {b2}{}{s}.", player.getScoreboardName(), gameMode.name), false);
    }
}
