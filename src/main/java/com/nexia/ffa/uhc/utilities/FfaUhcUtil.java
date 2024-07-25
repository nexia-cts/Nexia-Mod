package com.nexia.ffa.uhc.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import static com.nexia.ffa.uhc.utilities.FfaAreas.*;

public class FfaUhcUtil extends BaseFfaUtil {

    public static final FfaUhcUtil INSTANCE = new FfaUhcUtil();

    @Override
    public String getName() {
        return "UHC";
    }

    @Override
    public FfaGameMode getGameMode() {
        return FfaGameMode.UHC;
    }

    @Override
    public PlayerDataManager getDataManager() {
        return PlayerDataManager.getDataManager(NexiaCore.FFA_UHC_DATA_MANAGER);
    }

    @Override
    public ServerLevel getFfaWorld() {
        return ffaWorld;
    }

    @Override
    public World getNexusFfaWorld() {
        return nexusFfaWorld;
    }

    @Override
    public EntityPos getSpawn() {
        return spawn;
    }

    @Override
    public boolean isInFfaSpawn(NexiaPlayer player) {
        return FfaAreas.isInFfaSpawn(player);
    }

    @Override
    public AABB getFfaCorners() {
        return new AABB(ffaCorner1, ffaCorner2);
    }

    @Override
    public Minecraft.GameMode getMinecraftGameMode() {
        return Minecraft.GameMode.SURVIVAL;
    }

    @Override
    public void completeFiveTick(NexiaPlayer player) {
        if (wasInSpawn.contains(player.getUUID()) && !isInFfaSpawn(player)) {
            wasInSpawn.remove(player.getUUID());
            saveInventory(player);
            player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        }
    }

    public boolean beforeBuild(NexiaPlayer player, BlockPos blockPos) {
        if (player.getGameMode().equals(Minecraft.GameMode.CREATIVE)) return true;
        if (wasInSpawn.contains(player.getUUID())) return false;
        return blockPos.getY() < FfaAreas.buildLimitY;
    }

    @Override
    public void finishSendToSpawn(NexiaPlayer player) {
        setInventory(player);
        if(shouldResetMap) {
            ServerTime.scheduler.schedule(() -> FfaAreas.resetMap(true), 30);
            shouldResetMap = false;
        }
    }
}
