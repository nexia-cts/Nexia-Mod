package com.nexia.core.utilities.time;

import com.combatreforged.factory.api.FactoryAPI;
import com.combatreforged.factory.api.FactoryServer;
import com.combatreforged.factory.api.scheduler.TaskScheduler;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelsSpawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.fantasy.Fantasy;

public class ServerTime {

    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;

    public static MinecraftServer minecraftServer = null;

    public static ServerPlayer joinPlayer = null;

    public static ServerPlayer leavePlayer = null;

    public static FactoryServer factoryServer = null;

    public static FactoryAPI factoryAPI = null;

    public static TaskScheduler scheduler = null;

    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        minecraftServer = server;
        Main.server = server;

        fantasy = Fantasy.get(minecraftServer);
        LobbyUtil.setLobbyWorld(minecraftServer);
        DuelsSpawn.setDuelWorld(minecraftServer);
        FfaAreas.setFfaWorld(minecraftServer);

        DuelGameHandler.starting();
    }

    public static void stopServer() {
        try {
            for(ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                player.connection.disconnect(LegacyChatFormat.formatFail("The server is restarting!"));
            }
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
            case 4 -> {}
        }

    }

    static void everySecond() {
        totalSecondCount++;
    }

}
