package com.nexia.ffa.pot.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import net.kyori.adventure.text.Component;

public class FfaPotUtil extends BaseFfaUtil {
    public static final FfaPotUtil INSTANCE = new FfaPotUtil();

    public FfaPotUtil() {
        super(new PotFfaAreas());
    }

    @Override
    public String getName() {
        return "Pot";
    }

    @Override
    public FfaGameMode getGameMode() {
        return FfaGameMode.POT;
    }

    @Override
    public PlayerDataManager getDataManager() {
        return PlayerDataManager.getDataManager(NexiaCore.FFA_POT_DATA_MANAGER);
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
    public void finishSendToSpawn(NexiaPlayer player) {
        setInventory(player);
    }
}
