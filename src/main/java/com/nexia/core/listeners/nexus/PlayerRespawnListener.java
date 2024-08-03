package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import com.nexia.minigames.games.skywars.util.player.SkywarsPlayerData;
import com.nexia.nexus.api.event.player.PlayerRespawnEvent;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.api.world.util.Location;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class PlayerRespawnListener {
    public void registerListener() {
        PlayerRespawnEvent.BACKEND.register((respawnEvent) -> {
            NexiaPlayer player = new NexiaPlayer(respawnEvent.getPlayer());

            DuelsPlayerData duelsData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
            CorePlayerData data = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);

            if(duelsData.gameOptions == null) return;

            DuelsGame duelsGame = duelsData.gameOptions.duelsGame;
            TeamDuelsGame teamDuelsGame = duelsData.gameOptions.teamDuelsGame;


            if(data.gameMode == PlayerGameMode.SKYWARS) {
                Location respawn = new Location(0,100, 0, WorldUtil.getWorld(SkywarsGame.world));

                boolean isPlaying = ((SkywarsPlayerData)PlayerDataManager.getDataManager(NexiaCore.SKYWARS_DATA_MANAGER).get(player)).gameMode == SkywarsGameMode.PLAYING;
                ServerPlayer serverPlayer = PlayerUtil.getPlayerAttacker(player.unwrap());
                if(serverPlayer != null && !serverPlayer.equals(player.unwrap()) && isPlaying) {
                    respawn.setX(serverPlayer.getX());
                    respawn.setY(serverPlayer.getY());
                    respawn.setZ(serverPlayer.getZ());
                    respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                    respawnEvent.setSpawnpoint(respawn);
                }

                return;
            }

            if(data.gameMode == PlayerGameMode.BEDWARS)
                respawnEvent.setRespawnMode(Minecraft.GameMode.SURVIVAL);

            if(data.gameMode == PlayerGameMode.OITC)
                respawnEvent.setRespawnMode(Minecraft.GameMode.ADVENTURE);
            
            if(duelsGame != null && duelsGame.isEnding && duelsGame.winner != null) {
                player.getInventory().clear();
                LobbyUtil.giveItems(player);

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(duelsGame.winner.getLocation());
            } else if(teamDuelsGame != null && duelsData.duelOptions.duelsTeam != null) {
                player.getInventory().clear();
                LobbyUtil.giveItems(player);
                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);

                Location respawn = new Location(0,80, 0, WorldUtil.getWorld(teamDuelsGame.level));

                NexiaPlayer player1 = null;
                if(duelsData.duelOptions.duelsTeam.alive.isEmpty()) {
                    if(teamDuelsGame.team1 == duelsData.duelOptions.duelsTeam && !teamDuelsGame.team2.alive.isEmpty()) {
                        player1 = teamDuelsGame.team2.alive.get(new Random().nextInt(teamDuelsGame.team2.alive.size()));
                    } else if(teamDuelsGame.team2 == duelsData.duelOptions.duelsTeam && !teamDuelsGame.team1.alive.isEmpty()){
                        player1 = teamDuelsGame.team1.alive.get(new Random().nextInt(teamDuelsGame.team1.alive.size()));
                    }
                } else {
                    player1 = duelsData.duelOptions.duelsTeam.alive.get(new Random().nextInt(duelsData.duelOptions.duelsTeam.alive.size()));
                }

                if(player1 != null) {
                    respawn = player1.getLocation();
                }

                respawnEvent.setSpawnpoint(respawn);
            }
        });
    }
}
