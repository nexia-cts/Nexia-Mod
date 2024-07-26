package com.nexia.ffa.uhc.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;

import static com.nexia.ffa.uhc.utilities.UhcFfaAreas.shouldResetMap;

public class FfaUhcUtil extends BaseFfaUtil {

    public static final FfaUhcUtil INSTANCE = new FfaUhcUtil();

    public FfaUhcUtil() {
        super(new UhcFfaAreas());
    }

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
        return blockPos.getY() < UhcFfaAreas.buildLimitY;
    }

    @Override
    public void finishSendToSpawn(NexiaPlayer player) {
        setInventory(player);
        if(shouldResetMap) {
            ServerTime.scheduler.schedule(() -> UhcFfaAreas.resetMap(true), 30);
            shouldResetMap = false;
        }
    }
}
