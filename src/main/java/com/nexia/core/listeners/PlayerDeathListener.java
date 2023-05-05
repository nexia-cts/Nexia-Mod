package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerDeathEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDeathListener {
    public static void registerListener(){
        PlayerDeathEvent.BACKEND.register(playerDeathEvent -> {
            ListenerHelper.redirectDeathListener(playerDeathEvent);
        });
    }
}
