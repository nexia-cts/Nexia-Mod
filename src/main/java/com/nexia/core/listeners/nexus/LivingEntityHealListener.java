package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.nexus.api.event.entity.LivingEntityHealEvent;
import com.nexia.nexus.api.world.entity.player.Player;

public class LivingEntityHealListener {
    public void registerListener() {
        LivingEntityHealEvent.BACKEND.register(livingEntityHealEvent -> {
            if (!(livingEntityHealEvent.getEntity() instanceof Player player)) return;
            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

            if (FfaUhcUtil.INSTANCE.isFfaPlayer(nexiaPlayer) && livingEntityHealEvent.getCause() == LivingEntityHealEvent.HealCause.NATURAL_REGENERATION) {
                livingEntityHealEvent.setCancelled(true);
            }
        });
    }
}
