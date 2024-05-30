package com.nexia.core.listeners.factory;

import com.combatreforged.metis.api.event.player.PlayerDisconnectEvent;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.kyori.adventure.text.Component;

public class PlayerLeaveListener {
    public void registerListener() {
        PlayerDisconnectEvent.BACKEND.register(playerDisconnectEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerDisconnectEvent.getPlayer());
            processDisconnect(player);


            playerDisconnectEvent.setLeaveMessage(
                    Component.text("[").color(ChatFormat.lineColor)
                            .append(Component.text("-").color(ChatFormat.failColor))
                            .append(Component.text("] ").color(ChatFormat.lineColor))
                            .append(Component.text(player.getRawName()).color(ChatFormat.failColor))
            );
        });
    }



    private static void processDisconnect(NexiaPlayer player){
        if (BwUtil.isInBedWars(player)) BwPlayerEvents.leaveInBedWars(player);
        else if (FfaUtil.isFfaPlayer(player)) {
            FfaUtil.leaveOrDie(player, player.unwrap().getLastDamageSource(), true);
        }
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY) DuelGameHandler.leave(player, true);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.SKYWARS) SkywarsGame.leave(player);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC) OitcGame.leave(player);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.FOOTBALL) FootballGame.leave(player);

        com.nexia.ffa.classic.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.kits.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.uhc.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.sky.utilities.player.PlayerDataManager.removePlayerData(player);

        com.nexia.discord.utilities.player.PlayerDataManager.removePlayerData(player.getUUID());
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.oitc.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.football.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.skywars.util.player.PlayerDataManager.removePlayerData(player);


        //LobbyUtil.leaveAllGames(minecraftPlayer, true);

        PlayerDataManager.removePlayerData(player);
    }
}
