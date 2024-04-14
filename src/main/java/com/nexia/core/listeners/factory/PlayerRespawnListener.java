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
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
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
                respawnEvent.setSpawnpoint(new Location(duelsGame.winner.get().getX(), duelsGame.winner.get().getY(), duelsGame.winner.get().getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", WorldUtil.getWorldName(duelsGame.level).split(":")[1]))));
            } else if(teamDuelsGame != null && duelsData.duelsTeam != null) {
                factoryPlayer.getInventory().clear();
                LobbyUtil.giveItems(player);

                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);

                ServerPlayer player1;
                if(duelsData.duelsTeam.alive.isEmpty()) {
                    if(teamDuelsGame.team1 == duelsData.duelsTeam) {
                        player1 = teamDuelsGame.team2.alive.get(new Random().nextInt(teamDuelsGame.team2.alive.size())).get();
                    } else {
                        player1 = teamDuelsGame.team1.alive.get(new Random().nextInt(teamDuelsGame.team1.alive.size())).get();
                    }
                } else {
                    player1 = duelsData.duelsTeam.alive.get(new Random().nextInt(duelsData.duelsTeam.alive.size())).get();
                }


                respawnEvent.setSpawnpoint(new Location(player1.getX(), player1.getY(), player1.getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", WorldUtil.getWorldName(teamDuelsGame.level).split(":")[1]))));
            }
        });
    }
}
