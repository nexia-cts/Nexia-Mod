package com.nexia.core.listeners.factory;

import com.combatreforged.factory.api.event.player.PlayerHotbarDropItemEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDropItemListener {
    public static void registerListener(){
        PlayerHotbarDropItemEvent.BACKEND.register(playerDropItemEvent -> {

            Player player = playerDropItemEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            if (!EventUtil.dropItem(minecraftPlayer, playerDropItemEvent.getItemStack())) {
                playerDropItemEvent.setCancelled(true);
                ItemStackUtil.sendInventoryRefreshPacket(minecraftPlayer);
            }
        });
    }
}
