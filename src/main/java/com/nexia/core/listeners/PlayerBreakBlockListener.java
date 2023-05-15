package com.nexia.core.listeners;

import com.combatreforged.factory.api.event.player.PlayerBreakBlockEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.util.Location;
import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class PlayerBreakBlockListener {

    private static boolean isBed = false;
    public static void registerListener() {
        PlayerBreakBlockEvent.BACKEND.register(playerBreakBlockEvent -> {

            Player player = playerBreakBlockEvent.getPlayer();
            ServerPlayer minecraftPlayer = PlayerUtil.getMinecraftPlayer(player);

            Level level = minecraftPlayer.getLevel();

            Location blockLocation = playerBreakBlockEvent.getLocation();
            BlockPos blockPos = new BlockPos(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());

            isBed = BlockUtil.blockToText(level.getBlockState(blockPos)).endsWith("_bed");
        });
    }
}
