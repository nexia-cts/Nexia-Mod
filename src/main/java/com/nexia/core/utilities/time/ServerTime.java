package com.nexia.core.utilities.time;

import com.combatreforged.factory.api.FactoryAPI;
import com.combatreforged.factory.api.FactoryServer;
import com.combatreforged.factory.api.scheduler.TaskScheduler;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.minigames.GameHandler;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.areas.BwDimension;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.world.WorldUtil;
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

    public static ServerType serverType = null;

    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        ServerTime.minecraftServer = server;
        Main.server = server;

        serverType = ServerType.returnServer();
        fantasy = Fantasy.get(minecraftServer);

        LobbyUtil.setLobbyWorld(minecraftServer);

        FfaAreas.setFfaWorld(minecraftServer);
        com.nexia.ffa.kits.utilities.FfaAreas.setFfaWorld(minecraftServer);

        BwLoadShop.loadBedWarsShop(true);
        BwDimension.register();
        BwGame.firstTick();

        WorldUtil.deleteTempWorlds();

        SkywarsGame.firstTick();
        OitcGame.firstTick(minecraftServer);
        DuelGameHandler.starting();
    }

    public static void stopServer() {
        try {
            for(ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                player.connection.disconnect(LegacyChatFormat.formatFail("The server is restarting!"));
            }

            DuelGameHandler.starting();
            BwAreas.clearQueueBuild();

            WorldUtil.deleteTempWorlds();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void everyTick() {
        totalTickCount++;

        BwGame.tick();

        if (totalTickCount % 5 == 0) {
            FfaClassicUtil.fiveTick();
            FfaKitsUtil.fiveTick();
        }

        // Most second methods are also handled here to avoid too many methods from being executed at the same time
        switch (totalTickCount % 20) {
            case 0 -> everySecond();
            case 2 -> {
                FfaClassicUtil.ffaSecond();
                FfaKitsUtil.ffaSecond();
            }
            case 4 -> {}
            case 6 -> BwGame.bedWarsSecond();
        }
    }

    static void everySecond() {
        totalSecondCount++;
        OitcGame.second();
        SkywarsGame.second();
        GameHandler.second();
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
