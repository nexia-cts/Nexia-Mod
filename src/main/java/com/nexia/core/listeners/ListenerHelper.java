package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerDeathEvent;
import com.combatreforged.factory.api.world.damage.DamageData;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.server.level.ServerPlayer;

public class ListenerHelper {
    public static void redirectDeathListener(PlayerDeathEvent playerDeathEvent){
        Player player = playerDeathEvent.getPlayer();
        ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);

        if(data.gameMode == PlayerGameMode.FFA){
            FfaUtil.leaveOrDie(minecraftPlayer, playerDeathEvent, null);
        }
        if(data.gameMode == PlayerGameMode.BEDWARS){
            BwPlayerEvents.death(minecraftPlayer);
        }
        /*
        if(data.gameMode == PlayerGameMode.OITC){
            OitcGame.death(minecraftPlayer, playerDeathEvent, null);
        }

         */
        if(data.gameMode == PlayerGameMode.LOBBY){
            DuelsGame.death(minecraftPlayer, playerDeathEvent, null);
        }
    }
}
