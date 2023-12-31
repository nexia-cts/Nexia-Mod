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
import com.nexia.minigames.GameHandler;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcGameMode;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import com.nexia.world.WorldUtil;
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
            DuelsGame duelsGame = duelsData.duelsGame;
            TeamDuelsGame teamDuelsGame = duelsData.teamDuelsGame;

            
            if(data.gameMode == PlayerGameMode.OITC) {
                double[] respawn = {0, 100, 0};
                boolean isPlaying = com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(player).gameMode == OitcGameMode.PLAYING;
                ServerPlayer serverPlayer = PlayerUtil.getPlayerAttacker(player);
                if(serverPlayer != null && isPlaying) {
                    respawn[0] = serverPlayer.getX();
                    respawn[1] = serverPlayer.getY();
                    respawn[2] = serverPlayer.getZ();
                }


                if(isPlaying) {
                    GameHandler.showCountdown(player, 5);
                }
                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(new Location(respawn[0], respawn[1], respawn[2], ServerTime.factoryServer.getWorld(new Identifier("oitc", OitcGame.world.dimension().toString().replaceAll("]", "").split(":")[2]))));
                return;
            }

            if(data.gameMode == PlayerGameMode.SKYWARS) {
                double[] respawn = {0, 100, 0};
                boolean isPlaying = com.nexia.minigames.games.skywars.util.player.PlayerDataManager.get(player).gameMode == SkywarsGameMode.PLAYING;
                ServerPlayer serverPlayer = PlayerUtil.getPlayerAttacker(player);
                if(serverPlayer != null && isPlaying) {
                    respawn[0] = serverPlayer.getX();
                    respawn[1] = serverPlayer.getY();
                    respawn[2] = serverPlayer.getZ();
                }

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(new Location(respawn[0], respawn[1], respawn[2], WorldUtil.getWorld(SkywarsGame.world)));
            }
            
            if(duelsGame != null && duelsGame.isEnding && duelsGame.winner != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(new Location(duelsGame.winner.get().getX(), duelsGame.winner.get().getY(), duelsGame.winner.get().getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", duelsGame.level.dimension().toString().replaceAll("]", "").split(":")[2]))));
                //player.teleportTo(duelsGame.level, duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), 0, 0);
            } else if(teamDuelsGame != null && duelsData.duelsTeam != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                // duelsTeam.alive.get(new Random().nextInt(teamDuelsGame.winner.alive.size()))

                ServerPlayer player1;
                if(duelsData.duelsTeam.alive.isEmpty()) {
                    if(teamDuelsGame.team1 == duelsData.duelsTeam) {
                        player1 = teamDuelsGame.team2.alive.get(new Random().nextInt(teamDuelsGame.team2.alive.size()));
                    } else {
                        player1 = teamDuelsGame.team1.alive.get(new Random().nextInt(teamDuelsGame.team1.alive.size()));
                    }
                } else {
                    player1 = duelsData.duelsTeam.alive.get(new Random().nextInt(duelsData.duelsTeam.alive.size()));
                }


                respawnEvent.setSpawnpoint(new Location(player1.getX(), player1.getY(), player1.getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", teamDuelsGame.level.dimension().toString().replaceAll("]", "").split(":")[2]))));
                //player.teleportTo(duelsGame.level, duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), 0, 0);
            }

        });
    }
}
