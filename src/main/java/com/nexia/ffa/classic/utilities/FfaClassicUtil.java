package com.nexia.ffa.classic.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class FfaClassicUtil extends BaseFfaUtil {
    public static final FfaClassicUtil INSTANCE = new FfaClassicUtil();

    public FfaClassicUtil() {
        super(new ClassicFfaAreas());
    }

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
    public void completeFiveTick(NexiaPlayer player) {
        if(wasInSpawn.contains(player.getUUID()) && !isInFfaSpawn(player)){
            wasInSpawn.remove(player.getUUID());
            saveInventory(player);
            player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.", ChatFormat.normalColor)));
        }
    }

    @Override
    public void finishSendToSpawn(NexiaPlayer player) {
        setInventory(player);
    }
}
