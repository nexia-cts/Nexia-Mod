package com.nexia.ffa.classic.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.util.Location;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.TimerQueue;

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
    public boolean checkBot() {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return false;

        Player bot = ServerTime.nexusServer.getPlayer("femboy.ai");

        if (bot != null && getNexusFfaWorld().getPlayers().size() == 1) {
            bot.kill(); // despawns the bot
            return true;
        }

        if (bot == null && !getNexusFfaWorld().getPlayers().isEmpty()) {
            // spawn the bot
            ServerTime.nexusServer.runCommand("/player femboy.ai spawn", 4, false);

            bot = ServerTime.nexusServer.getPlayer("femboy.ai");
            bot.runCommand("ffa classic");

            ServerTime.nexusServer.runCommand("/player femboy.ai move forward", 4, false);

            TimerQueue<MinecraftServer> timerQueue = ServerTime.minecraftServer.getWorldData().overworldData().getScheduledEvents();
            timerQueue.schedule("core:bot/look", 1, new FunctionCallback(new ResourceLocation("core:bot/look")));
            timerQueue.schedule("core:bot/attack", 7, new FunctionCallback(new ResourceLocation("core:bot/look")));
            timerQueue.schedule("core:bot/attack2", 10, new FunctionCallback(new ResourceLocation("core:bot/look")));
            timerQueue.schedule("core:bot/strafe", 4, new FunctionCallback(new ResourceLocation("core:bot/look")));

            bot.addTag("bot");
            bot.teleport(new Location(0.5, 128, 0.5, getNexusFfaWorld()));
            ServerTime.nexusServer.runCommand("/player femboy.ai move forward", 4, false);
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
