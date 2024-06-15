package com.nexia.minigames.games.bedwars.util;

import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.logging.log4j.LogManager;

public class BwPlayerTracker {

    public static void trackerSecond() {
        for (ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            if (BwUtil.isBedWarsPlayer(player)) {
                trackCompass(player);
            }
        }
    }

    // Update all tracker compasses
    public static void trackCompass(ServerPlayer player) {

        if (!BwUtil.isBedWarsPlayer(player)) return;

        Inventory inventory = player.inventory;
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!isTrackerCompass(itemStack)) continue;
            trackClosestPlayer(player, itemStack);
        }

    }

    static boolean isTrackerCompass(ItemStack itemStack) {

        if (!itemStack.getItem().toString().equals("compass")) return false;

        CompoundTag displayTag = itemStack.getTagElement("display");
        if (displayTag == null) return false;

        Tag lore = displayTag.get("Lore");
        return lore != null && lore.toString().contains("Bedwars Player Tracker");
    }

    static void trackClosestPlayer(ServerPlayer player, ItemStack itemStack) {

        MutablePair<ServerPlayer, Double> tracked = getClosestPlayer(player);
        ServerPlayer trackedPlayer = tracked.left;
        int distance = tracked.right.intValue();
        if (trackedPlayer == null) return;

        itemStack.setHoverName(new TextComponent(
                "\247bTracking: " + trackedPlayer.getScoreboardName() +
                "\2477 | " + "\247eDistance: " + distance));

        BlockPos blockPos = new BlockPos(trackedPlayer.getX(), trackedPlayer.getY(), trackedPlayer.getZ());
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
        Level.RESOURCE_KEY_CODEC.encodeStart(
                        NbtOps.INSTANCE, player.level.dimension()).resultOrPartial(LogManager.getLogger()::error)
                .ifPresent(tag -> compoundTag.put("LodestoneDimension", tag));
        compoundTag.putBoolean("LodestoneTracked", true);

    }

    static MutablePair<ServerPlayer, Double> getClosestPlayer(ServerPlayer player) {
        ServerPlayer closestPlayer = null;
        double closestPos = Double.MAX_VALUE;

        for (ServerPlayer trackable : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            if (BwUtil.isBedWarsPlayer(trackable) && !trackable.getUUID().equals(player.getUUID())) {

                double distance = (trackable.getX() - player.getX()) * (trackable.getX() - player.getX()) +
                        (trackable.getZ() - player.getZ()) * (trackable.getZ() - player.getZ());
                if (distance < closestPos) {
                    closestPlayer = trackable;
                    closestPos = distance;
                }

            }
        }

        closestPos = Math.sqrt(closestPos);
        return new MutablePair<>(closestPlayer, closestPos);
    }

}
