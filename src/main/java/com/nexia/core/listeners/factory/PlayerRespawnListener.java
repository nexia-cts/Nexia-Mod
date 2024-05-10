package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerRespawnEvent;
import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.combatreforged.factory.api.world.util.Location;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import com.nexia.core.utilities.world.WorldUtil;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

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

            PlayerData duelsData = PlayerDataManager.get(player);
            com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);

            if(duelsData.gameOptions == null) return;

            DuelsGame duelsGame = duelsData.gameOptions.duelsGame;
            TeamDuelsGame teamDuelsGame = duelsData.gameOptions.teamDuelsGame;

            CustomDuelsGame customDuelsGame = duelsData.gameOptions.customDuelsGame;
            CustomTeamDuelsGame customTeamDuelsGame = duelsData.gameOptions.customTeamDuelsGame;


            if(data.gameMode == PlayerGameMode.SKYWARS) {
                Location respawn = new Location(0,100, 0, WorldUtil.getWorld(SkywarsGame.world));

                boolean isPlaying = com.nexia.minigames.games.skywars.util.player.PlayerDataManager.get(player).gameMode == SkywarsGameMode.PLAYING;
                ServerPlayer serverPlayer = PlayerUtil.getPlayerAttacker(player);
                if(serverPlayer != null && serverPlayer != player && isPlaying) {
                    respawn.setX(serverPlayer.getX());
                    respawn.setY(serverPlayer.getY());
                    respawn.setZ(serverPlayer.getZ());
                }

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(respawn);
                return;
            }
            
            if(duelsGame != null && duelsGame.isEnding && duelsGame.winner != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(new Location(duelsGame.winner.get().getX(), duelsGame.winner.get().getY(), duelsGame.winner.get().getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", WorldUtil.getIdentifierWorldName(duelsGame.level).getId()))));
            } else if(teamDuelsGame != null && duelsData.duelOptions.duelsTeam != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);
                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);

                Location respawn = new Location(0,80, 0, ServerTime.factoryServer.getWorld(new Identifier("duels", WorldUtil.getIdentifierWorldName(teamDuelsGame.level).getId())));

                ServerPlayer player1 = null;
                if(duelsData.duelOptions.duelsTeam.alive.isEmpty()) {
                    if(teamDuelsGame.team1 == duelsData.duelOptions.duelsTeam && !teamDuelsGame.team2.alive.isEmpty()) {
                        player1 = teamDuelsGame.team2.alive.get(new Random().nextInt(teamDuelsGame.team2.alive.size())).get();
                    } else if(teamDuelsGame.team2 == duelsData.duelOptions.duelsTeam && !teamDuelsGame.team1.alive.isEmpty()){
                        player1 = teamDuelsGame.team1.alive.get(new Random().nextInt(teamDuelsGame.team1.alive.size())).get();
                    }

                    if(player1 != null) {
                        respawn.setX(player1.getX());
                        respawn.setY(player1.getY());
                        respawn.setZ(player1.getZ());
                    }

                } else {
                    player1 = duelsData.duelOptions.duelsTeam.alive.get(new Random().nextInt(duelsData.duelOptions.duelsTeam.alive.size())).get();
                }


                respawnEvent.setSpawnpoint(respawn);
                //player.teleportTo(duelsGame.level, duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), 0, 0);
            } else if(customDuelsGame != null && customDuelsGame.isEnding && customDuelsGame.winner != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(new Location(customDuelsGame.winner.get().getX(), customDuelsGame.winner.get().getY(), customDuelsGame.winner.get().getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", WorldUtil.getIdentifierWorldName(customDuelsGame.level).getId()))));
                //player.teleportTo(duelsGame.level, duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), 0, 0);
            } else if(customTeamDuelsGame != null && duelsData.duelOptions.duelsTeam != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);
                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);

                Location respawn = new Location(0,80, 0, ServerTime.factoryServer.getWorld(new Identifier("duels", WorldUtil.getIdentifierWorldName(customTeamDuelsGame.level).getId()))));

                ServerPlayer player1 = null;
                if(duelsData.duelOptions.duelsTeam.alive.isEmpty()) {
                    if(customTeamDuelsGame.team1 == duelsData.duelOptions.duelsTeam && !customTeamDuelsGame.team2.alive.isEmpty()) {
                        player1 = customTeamDuelsGame.team2.alive.get(new Random().nextInt(customTeamDuelsGame.team2.alive.size())).get();
                    } else if (customTeamDuelsGame.team2 == duelsData.duelOptions.duelsTeam && !customTeamDuelsGame.team1.alive.isEmpty()) {
                        player1 = customTeamDuelsGame.team1.alive.get(new Random().nextInt(customTeamDuelsGame.team1.alive.size())).get();
                    }

                    if(player1 != null) {
                        respawn.setX(player1.getX());
                        respawn.setY(player1.getY());
                        respawn.setZ(player1.getZ());
                    }


                } else {
                    player1 = duelsData.duelOptions.duelsTeam.alive.get(new Random().nextInt(duelsData.duelOptions.duelsTeam.alive.size())).get();
                }



                respawnEvent.setSpawnpoint(respawn);
            }

        });
    }
}
