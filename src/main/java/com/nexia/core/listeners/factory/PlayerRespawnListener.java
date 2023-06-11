package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerRespawnEvent;
import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.combatreforged.factory.api.world.util.Location;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;

public class PlayerRespawnListener {
    public static void registerListener(){
        PlayerRespawnEvent.BACKEND.register((respawnEvent) -> {
            Player factoryPlayer = respawnEvent.getPlayer();
            if(factoryPlayer == null && respawnEvent.getEntity() instanceof Player) {
                factoryPlayer = (Player) respawnEvent.getEntity();
            }

            if(factoryPlayer == null) return;

            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(factoryPlayer.getUUID());
            if(player == null) return;

            DuelsGame duelsGame = PlayerDataManager.get(player).duelsGame;

            if(duelsGame != null) {
                LobbyUtil.leaveAllGames(duelsGame.p1, true);
                LobbyUtil.leaveAllGames(duelsGame.p2, true);
            }
        });
    }
}
