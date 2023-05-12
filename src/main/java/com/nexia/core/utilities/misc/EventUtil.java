package com.nexia.core.utilities.misc;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.utilities.FfaUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EventUtil {

    public static boolean dropItem(Player player, ItemStack itemStack) {

        return !FfaUtil.isFfaPlayer(player) || !LobbyUtil.isLobbyWorld(player.level);
    }


}
