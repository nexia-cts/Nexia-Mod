package com.nexia.core.utilities.time;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.ffa.classic.utilities.ClassicFfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.kits.utilities.KitFfaAreas;
import com.nexia.ffa.pot.utilities.FfaPotUtil;
import com.nexia.ffa.pot.utilities.PotFfaAreas;
import com.nexia.ffa.sky.SkyFfaBlocks;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.sky.utilities.SkyFfaAreas;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.ffa.uhc.utilities.UhcFfaAreas;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.areas.BwDimension;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import com.nexia.minigames.games.bridge.BridgeGame;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.nexus.api.NexusAPI;
import com.nexia.nexus.api.NexusServer;
import com.nexia.nexus.api.scheduler.TaskScheduler;
import com.nexia.nexus.api.world.entity.player.Player;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.fantasy.Fantasy;

public class ServerTime {
    public static ServerPlayer leavePlayer = null;

    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;

    public static MinecraftServer minecraftServer = null;

    public static NexusServer nexusServer = null;

    public static NexusAPI nexusAPI = null;

    public static TaskScheduler scheduler = null;

    public static ServerType serverType = null;

    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        ServerTime.minecraftServer = server;

        serverType = ServerType.returnServer();
        fantasy = Fantasy.get(minecraftServer);

        NexiaRank.setupRanks(server);

        LobbyUtil.setLobbyWorld(minecraftServer);
        WorldUtil.setVoidWorld(minecraftServer);

        ClassicFfaAreas.setFfaWorld(server);
        KitFfaAreas.setFfaWorld(server);
        PotFfaAreas.setFfaWorld(server);
        SkyFfaAreas.setFfaWorld(server);
        UhcFfaAreas.setFfaWorld(server);

        BwLoadShop.loadBedWarsShop(true);
        BwDimension.register();
        BwGame.firstTick();
        FootballGame.firstTick();
        BridgeGame.firstTick();
        WorldUtil.deleteTempWorlds();

        SkywarsGame.firstTick();
        OitcGame.firstTick();
        BridgeGame.firstTick();
        DuelGameHandler.starting();
    }

    public static void stopServer() {
        try {
            for (Player player : ServerTime.nexusServer.getPlayers()) {
                player.disconnect(ChatFormat.nexiaMessage.append(Component.text("The server is restarting!", ChatFormat.Minecraft.white)));
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
        BridgeGame.tick();
        OitcGame.tick();
        BridgeGame.tick();

        if (totalTickCount % 5 == 0) {
            FfaClassicUtil.INSTANCE.fiveTick();
            FfaKitsUtil.INSTANCE.fiveTick();
            FfaPotUtil.INSTANCE.fiveTick();
            FfaSkyUtil.INSTANCE.fiveTick();
            FfaUhcUtil.INSTANCE.fiveTick();
        }

        // Most second methods are also handled here to avoid too many methods from being executed at the same time
        switch (totalTickCount % 20) {
            case 0 -> everySecond();
            case 2 -> {
            }
            case 4 -> {
            }
            case 6 -> BwGame.bedWarsSecond();
        }
    }

    static void everySecond() {
        totalSecondCount++;
        OitcGame.second();
        FootballGame.second();
        BridgeGame.second();
        SkywarsGame.second();
        BridgeGame.second();
        try {
            for (DuelsGame game : DuelGameHandler.duelsGames) {
                if (game == null) return;
                game.duelSecond();
            }
        } catch (Exception ignored) {
        }

        try {
            for (TeamDuelsGame game : DuelGameHandler.teamDuelsGames) {
                if (game == null) return;
                game.duelSecond();
            }
        } catch (Exception ignored) {
        }


        if (totalSecondCount % 3600 == 0 && !UhcFfaAreas.shouldResetMap) {
            UhcFfaAreas.shouldResetMap = true;
        }
    }

}
