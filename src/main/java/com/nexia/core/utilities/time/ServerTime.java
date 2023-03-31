package com.nexia.core.utilities.time;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.DuelsSpawn;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcSpawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.fantasy.Fantasy;

public class ServerTime {

    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;

    public static MinecraftServer minecraftServer = null;

    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        minecraftServer = server;
        Main.server = server;

        fantasy = Fantasy.get(minecraftServer);
        LobbyUtil.setLobbyWorld(minecraftServer);
        DuelsSpawn.setDuelWorld(minecraftServer);
        FfaAreas.setFfaWorld(minecraftServer);
        OitcGame.firstTick(minecraftServer);

        BwGame.firstTick();
        DuelsGame.starting();
    }

    public static void stopServer() {
        try {
            for(ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                player.connection.disconnect(ChatFormat.formatFail("The server is restarting!"));
            }
            DuelsGame.starting();
            BwAreas.clearQueueBuild();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void everyTick() {
        totalTickCount++;

        BwGame.tick();

        if (totalTickCount % 5 == 0) {
            FfaUtil.fiveTick();
        }

        // Most second methods are also handled here to avoid too many methods from being executed at the same time
        switch (totalTickCount % 20) {
            case 0 -> everySecond();
            case 2 -> FfaUtil.ffaSecond();
            case 4 -> {}
            case 6 -> BwGame.bedWarsSecond();
        }

    }

    static void everySecond() {
        totalSecondCount++;
        OitcGame.second();
    }

}
