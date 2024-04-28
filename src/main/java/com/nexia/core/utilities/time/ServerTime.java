package com.nexia.core.utilities.time;

import com.combatreforged.factory.api.FactoryAPI;
import com.combatreforged.factory.api.FactoryServer;
import com.combatreforged.factory.api.scheduler.TaskScheduler;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.SkyFfaBlocks;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.areas.BwDimension;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.core.utilities.world.WorldUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.fantasy.Fantasy;

public class ServerTime {
    public static ServerPlayer leavePlayer = null;

    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;

    public static MinecraftServer minecraftServer = null;

    public static FactoryServer factoryServer = null;

    public static FactoryAPI factoryAPI = null;

    public static TaskScheduler scheduler = null;

    public static ServerType serverType = null;

    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        ServerTime.minecraftServer = server;

        serverType = ServerType.returnServer();
        fantasy = Fantasy.get(minecraftServer);

        LobbyUtil.setLobbyWorld(minecraftServer);
        WorldUtil.setVoidWorld(minecraftServer);

        FfaAreas.setFfaWorld(minecraftServer);
        com.nexia.ffa.kits.utilities.FfaAreas.setFfaWorld(minecraftServer);
        com.nexia.ffa.uhc.utilities.FfaAreas.setFfaWorld(minecraftServer);
        com.nexia.ffa.sky.utilities.FfaAreas.setFfaWorld(minecraftServer);

        BwLoadShop.loadBedWarsShop(true);
        BwDimension.register();
        BwGame.firstTick();
        FootballGame.firstTick();
        WorldUtil.deleteTempWorlds();

        SkywarsGame.firstTick();
        OitcGame.firstTick();
        DuelGameHandler.starting();
    }

    public static void stopServer() {
        try {
            for(ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                player.connection.disconnect(LegacyChatFormat.formatFail("The server is restarting!"));
            }

            DuelGameHandler.starting();
            BwAreas.clearQueueBuild();
            SkyFfaBlocks.clearAllBlocks();

            WorldUtil.deleteTempWorlds();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void everyTick() {
        totalTickCount++;

        BwGame.tick();
        SkyFfaBlocks.tick();
        FootballGame.tick();
        OitcGame.tick();

        if (totalTickCount % 5 == 0) {
            FfaClassicUtil.fiveTick();
            FfaKitsUtil.fiveTick();
            FfaSkyUtil.fiveTick();
            FfaUhcUtil.fiveTick();
        }

        // Most second methods are also handled here to avoid too many methods from being executed at the same time
        switch (totalTickCount % 20) {
            case 0 -> everySecond();
            case 2 -> {
                FfaClassicUtil.ffaSecond();
                FfaKitsUtil.ffaSecond();
                FfaUhcUtil.ffaSecond();
            }
            case 4 -> {}
            case 6 -> BwGame.bedWarsSecond();
        }
    }

    static void everySecond() {
        totalSecondCount++;
        OitcGame.second();
        FootballGame.second();
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

        try {
            for (CustomDuelsGame game : DuelGameHandler.customDuelsGames) {
                if (game == null) return;
                game.duelSecond();
            }
        } catch (Exception ignored) { }

        try {
            for (CustomTeamDuelsGame game : DuelGameHandler.customTeamDuelsGames) {
                if (game == null) return;
                game.duelSecond();
            }
        } catch (Exception ignored) { }


        if(totalSecondCount % 3600 == 0 && !com.nexia.ffa.uhc.utilities.FfaAreas.shouldResetMap) {
            com.nexia.ffa.uhc.utilities.FfaAreas.shouldResetMap = true;
        }
    }

}
