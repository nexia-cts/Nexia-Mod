package com.nexia.ffa.classic.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.nexus.api.world.World;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static com.nexia.ffa.classic.utilities.FfaAreas.*;

public class FfaClassicUtil extends BaseFfaUtil {
    public static final FfaClassicUtil INSTANCE = new FfaClassicUtil();
    @Override
    public String getName() {
        return "Classic";
    }

    @Override
    public FfaGameMode getGameMode() {
        return FfaGameMode.CLASSIC;
    }

    @Override
    public PlayerDataManager getDataManager() {
        return PlayerDataManager.getDataManager(NexiaCore.FFA_CLASSIC_DATA_MANAGER);
    }
    
    @Override
    public boolean checkBot(List<ServerPlayer> players) {
        ServerPlayer bot = null;

        for(ServerPlayer player : players) {
            if(player.getScoreboardName().equals("femboy.ai")) {
                bot = player;
                break;
            }
        }

        if(bot != null && players.size() == 1) {
            bot.kill(); // despawns the bot
            return true;
        }

        if(bot == null && !players.isEmpty()) {
            ServerTime.nexusServer.runCommand("/function core:bot/bot", 4, false); // spawns the bot
            return false;
        }

        return false;
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
    public void completeFiveTick(NexiaPlayer player) {
        if(wasInSpawn.contains(player.getUUID()) && !isInFfaSpawn(player)){
            wasInSpawn.remove(player.getUUID());
            saveInventory(player);
            player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        }
    }

    @Override
    public boolean isInFfaSpawn(NexiaPlayer player) {
        return FfaAreas.isInFfaSpawn(player);
    }

    @Override
    public BlockPos[] getFfaCorners() {
        return new BlockPos[]{ffaCorner1, ffaCorner2};
    }

    @Override
    public void finishSendToSpawn(NexiaPlayer player) {
        setInventory(player);
    }
}
