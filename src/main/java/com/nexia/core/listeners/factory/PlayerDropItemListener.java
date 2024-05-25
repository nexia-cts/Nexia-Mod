package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerHotbarDropItemEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.players.AccuratePlayer;

public class PlayerDropItemListener {
    public static void registerListener(){
        PlayerHotbarDropItemEvent.BACKEND.register(playerDropItemEvent -> {

            Player player = playerDropItemEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);
            NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(minecraftPlayer));

            if (!EventUtil.dropItem(nexiaPlayer, playerDropItemEvent.getItemStack())) {
                playerDropItemEvent.setCancelled(true);
                nexiaPlayer.refreshInventory();
                return;
            }
        });
    }
}
