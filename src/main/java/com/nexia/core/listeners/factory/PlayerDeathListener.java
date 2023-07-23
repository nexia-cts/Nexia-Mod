package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerDeathEvent;
import com.combatreforged.factory.api.event.player.PlayerHotbarDropItemEvent;
import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.combatreforged.factory.api.world.util.Location;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class PlayerDeathListener {
    public static void registerListener(){
        PlayerDeathEvent.BACKEND.register(playerDeathEvent -> {
            Player factoryPlayer = playerDeathEvent.getPlayer();
            if(factoryPlayer == null && playerDeathEvent.getEntity() instanceof Player) {
                factoryPlayer = (Player) playerDeathEvent.getEntity();
            }

            if(factoryPlayer == null) return;

            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(factoryPlayer.getUUID());

            if(player == null) return;
            PlayerData duelsData = PlayerDataManager.get(player);
            DuelsGame duelsGame = duelsData.duelsGame;
            TeamDuelsGame teamDuelsGame = duelsData.teamDuelsGame;

            if(duelsGame != null && duelsGame.isEnding && duelsGame.winner != null) {
                playerDeathEvent.setDropEquipment(true);
                playerDeathEvent.setDropExperience(true);
                playerDeathEvent.setDropLoot(true);
            } else if(teamDuelsGame != null && duelsData.duelsTeam != null) {
                playerDeathEvent.setDropEquipment(true);
                playerDeathEvent.setDropExperience(true);
                playerDeathEvent.setDropLoot(true);
            }
        });
    }
}
