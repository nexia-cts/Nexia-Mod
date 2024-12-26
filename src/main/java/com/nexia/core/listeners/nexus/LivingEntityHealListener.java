package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.nexus.api.event.entity.LivingEntityHealEvent;

public class LivingEntityHealListener {
    public void registerListener() {
        LivingEntityHealEvent.BACKEND.register(livingEntityHealEvent -> {
            if (!(livingEntityHealEvent.getEntity() instanceof NexiaPlayer nexiaPlayer)) return;

            if (FfaUhcUtil.INSTANCE.isFfaPlayer(nexiaPlayer) && livingEntityHealEvent.getCause() == LivingEntityHealEvent.HealCause.NATURAL_REGENERATION) {
                livingEntityHealEvent.setCancelled(true);
            }
        });
    }
}
