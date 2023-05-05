package com.nexia.core.utilities.time;

import com.combatreforged.factory.api.FactoryAPI;
import com.combatreforged.factory.api.FactoryServer;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.loader.CommandLoader;
import com.nexia.core.loader.ListenerLoader;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.areas.BwDimension;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.DuelsSpawn;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.fantasy.Fantasy;

public class ServerTime {

    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;

    public static MinecraftServer minecraftServer = null;

    public static FactoryServer factoryServer = null;

    public static FactoryAPI factoryAPI = null;



    public static Fantasy fantasy = null;

    public static void firstTick(MinecraftServer server) {
        minecraftServer = server;
        Main.server = server;

        factoryAPI = FactoryAPI.getInstance();
        factoryServer = factoryAPI.getServer();

        fantasy = Fantasy.get(minecraftServer);
        LobbyUtil.setLobbyWorld(minecraftServer);
        DuelsSpawn.setDuelWorld(minecraftServer);
        FfaAreas.setFfaWorld(minecraftServer);
        OitcGame.firstTick(minecraftServer);

        CommandLoader.registerCommands();
        ListenerLoader.registerListeners();
        BwLoadShop.loadBedWarsShop(true);
        BwDimension.register();
        BwGame.firstTick();
        DuelGameHandler.starting();
    }

    public static void stopServer() {
        try {
            for(ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                player.connection.disconnect(LegacyChatFormat.formatFail("The server is restarting!"));
            }
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
