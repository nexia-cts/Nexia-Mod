package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerRespawnEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.server.level.ServerPlayer;

public class PlayerRespawnListener {
    public static void registerListener(){
        PlayerRespawnEvent.BACKEND.register(playerRespawnEvent -> {
            Player player = playerRespawnEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            //playerRespawnEvent.setRespawnMode(Minecraft.GameMode.ADVENTURE);

            if(minecraftPlayer != null && BwUtil.isInBedWars(minecraftPlayer)) BwPlayerEvents.respawned(minecraftPlayer);
        });
    }
}
