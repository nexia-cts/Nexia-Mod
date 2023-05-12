package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerHotbarDropItemEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.FfaUtil;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDropItemListener {
    public static void registerListener(){
        PlayerHotbarDropItemEvent.BACKEND.register(playerDropItemEvent -> {

            Player player = playerDropItemEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            if (FfaUtil.isFfaPlayer(minecraftPlayer)) {
                playerDropItemEvent.setCancelled(true);
                return;
            }
            if(LobbyUtil.isLobbyWorld(minecraftPlayer.getLevel())){
                playerDropItemEvent.setCancelled(true);
            }
        });
    }
}
