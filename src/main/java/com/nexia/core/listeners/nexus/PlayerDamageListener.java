package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.nexus.api.event.entity.LivingEntityDamageEvent;
import com.nexia.nexus.api.world.damage.DamageData;
import com.nexia.nexus.api.world.entity.player.Player;

public class PlayerDamageListener {
    public void registerListener() {
        LivingEntityDamageEvent.BACKEND.register(livingEntityDamageEvent -> {
            if(!(livingEntityDamageEvent.getLivingEntity() instanceof Player nexusPlayer)) return;
            NexiaPlayer player = new NexiaPlayer(nexusPlayer);
            DamageData damageData = livingEntityDamageEvent.getCause();

            for (BaseFfaUtil util : BaseFfaUtil.ffaUtils) {
                if (util.isFfaPlayer(player) && !util.beforeDamage(player, damageData)) {
                    livingEntityDamageEvent.setCancelled(true);
                    return;
                }
            }

            if(LobbyUtil.isLobbyWorld(player.getWorld()) && damageData.getType().equals(DamageData.Type.VOID)) {
                LobbyUtil.lobbySpawn.teleportPlayer(LobbyUtil.nexusLobbyWorld, player);
                player.teleport(LobbyUtil.nexusLobbyLocation);
                livingEntityDamageEvent.setCancelled(true);
                return;
            }

            if (player.hasTag(LobbyUtil.NO_DAMAGE_TAG)) {
                livingEntityDamageEvent.setCancelled(true);
                return;
            }

            Player attacker = PlayerUtil.getPlayerAttacker(damageData);

            if(attacker != null) {
                if(attacker.hasTag(LobbyUtil.NO_DAMAGE_TAG)) {
                    livingEntityDamageEvent.setCancelled(true);
                    return;
                }
            }

            if(damageData.getType().equals(DamageData.Type.VOID) && (!player.isInGameMode(PlayerGameMode.LOBBY) || player.isInGameMode(PlayerGameMode.FFA))) {
                livingEntityDamageEvent.setDamage(1000);
                return;
            }
        });
    }
}
