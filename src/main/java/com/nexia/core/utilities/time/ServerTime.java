package com.nexia.core.utilities.time;

import com.combatreforged.factory.api.FactoryAPI;
import com.combatreforged.factory.api.FactoryServer;
import com.combatreforged.factory.api.scheduler.TaskScheduler;
import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.world.WorldUtil;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.fantasy.Fantasy;

import java.util.ArrayList;
import java.util.List;

public class ServerTime {

    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;


    public static MinecraftServer minecraftServer = null;

    public static ServerPlayer joinPlayer = null;

    public static ServerPlayer leavePlayer = null;

    public static FactoryServer factoryServer = null;

    public static FactoryAPI factoryAPI = null;

    public static ServerType serverType = null;

    public static TaskScheduler scheduler = null;

    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        minecraftServer = server;
        Main.server = server;

        ServerTime.serverType = ServerType.returnServer();

        fantasy = Fantasy.get(minecraftServer);
        LobbyUtil.setLobbyWorld(minecraftServer);
        FfaAreas.setFfaWorld(minecraftServer);

        List<Identifier> toDelete = new ArrayList<>();

        for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            String[] split = level.dimension().toString().replaceAll("]", "").split(":");
            if (split[1].toLowerCase().contains("duels")) {
                toDelete.add(new Identifier("duels", split[2]));
            }
            if (split[1].toLowerCase().contains("skywars")) {
                toDelete.add(new Identifier("skywars", split[2]));
            }
        }

        for (Identifier deletion : toDelete) {
            WorldUtil.deleteWorld(deletion);
        }


        SkywarsGame.firstTick();
        DuelGameHandler.starting();
    }

    public static void stopServer() {
        try {
            for(ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                player.connection.disconnect(LegacyChatFormat.formatFail("The server is restarting!"));
            }
            List<Identifier> toDelete = new ArrayList<>();

            for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
                String[] split = level.dimension().toString().replaceAll("]", "").split(":");
                if (split[1].toLowerCase().contains("duels")) {
                    toDelete.add(new Identifier("duels", split[2]));
                }
                if (split[1].toLowerCase().contains("skywars")) {
                    toDelete.add(new Identifier("skywars", split[2]));
                }
            }

            for (Identifier deletion : toDelete) {
                WorldUtil.deleteWorld(deletion);
            }
            DuelGameHandler.starting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void everyTick() {
        totalTickCount++;
        if (totalTickCount % 5 == 0) {
            FfaUtil.fiveTick();
        }

        // Most second methods are also handled here to avoid too many methods from being executed at the same time
        switch (totalTickCount % 20) {
            case 0 -> everySecond();
            case 2 -> FfaUtil.ffaSecond();
        }
    }

    static void everySecond() {
        totalSecondCount++;
        SkywarsGame.second();
        try {
            for (DuelsGame game : DuelGameHandler.duelsGames) {
                if (game == null) return;
                game.duelSecond();
            }
        } catch (Exception ignored) { }

        try {
            for (TeamDuelsGame game : DuelGameHandler.teamDuelsGames) {
                if (game == null) return;
                game.duelSecond();
            }
        } catch (Exception ignored) { }
    }
}
