package com.nexia.core.utilities.misc;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.duels.DuelsSpawn;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EventUtil {

    public static boolean dropItem(ServerPlayer player, ItemStack itemStack) {

        return !FfaUtil.isFfaPlayer(player) || !DuelsSpawn.isInHub(player) || !LobbyUtil.isLobbyWorld(player.getLevel());
    }


}
