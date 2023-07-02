package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerRespawnEvent;
import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.combatreforged.factory.api.world.util.Location;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
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
            DuelsGame duelsGame = PlayerDataManager.get(player).duelsGame;
            TeamDuelsGame teamDuelsGame = PlayerDataManager.get(player).teamDuelsGame;

            if(duelsGame != null && duelsGame.isEnding && duelsGame.winner != null) {
                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);
                respawnEvent.setSpawnpoint(new Location(duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", duelsGame.level.dimension().toString().replaceAll("]", "").split(":")[2]))));
                //player.teleportTo(duelsGame.level, duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), 0, 0);
            } else if(teamDuelsGame != null && teamDuelsGame.isEnding && teamDuelsGame.winner != null) {
                respawnEvent.setRespawnMode(Minecraft.GameMode.SPECTATOR);

                ServerPlayer player1 = teamDuelsGame.winner.alive.get(new Random().nextInt(teamDuelsGame.winner.alive.size()));

                respawnEvent.setSpawnpoint(new Location(player1.getX(), player1.getY(), player1.getZ(), ServerTime.factoryServer.getWorld(new Identifier("duels", teamDuelsGame.level.dimension().toString().replaceAll("]", "").split(":")[2]))));
                //player.teleportTo(duelsGame.level, duelsGame.winner.getX(), duelsGame.winner.getY(), duelsGame.winner.getZ(), 0, 0);
            }

        });
    }
}
