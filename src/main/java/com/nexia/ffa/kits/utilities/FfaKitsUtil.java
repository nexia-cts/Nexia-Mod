package com.nexia.ffa.kits.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.gui.ffa.KitGUI;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.ffa.kits.FfaKit;
import com.nexia.ffa.kits.utilities.player.KitFFAPlayerData;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.api.world.util.Location;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import static com.nexia.ffa.kits.utilities.FfaAreas.*;

public class FfaKitsUtil extends BaseFfaUtil {
    public static final FfaKitsUtil INSTANCE = new FfaKitsUtil();
    @Override
    public String getName() {
        return "Kits";
    }

    @Override
    public FfaGameMode getGameMode() {
        return FfaGameMode.KITS;
    }

    @Override
    public PlayerDataManager getDataManager() {
        return PlayerDataManager.getDataManager(NexiaCore.FFA_KITS_DATA_MANAGER);
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
    public Location getRespawnLocation() {
        return nexusFfaLocation;
    }

    @Override
    public void doPreKill(NexiaPlayer attacker, NexiaPlayer player) {
        clearProjectiles(attacker);
    }

    @Override
    public void completeFiveTick(NexiaPlayer player) {
        if(!isInFfaSpawn(player) && ((KitFFAPlayerData) PlayerDataManager.getDataManager(NexiaCore.FFA_KITS_DATA_MANAGER).get(player)).kit == null) {
            player.sendTitle(Title.title(Component.text("No kit selected!").color(ChatFormat.failColor), Component.text("You need to select a kit!").color(ChatFormat.failColor)));
            player.playSound(Minecraft.Sound.NOTE_BLOCK_DIDGERIDOO, 10, 1);
            sendToSpawn(player);
            return;
        }

        if(wasInSpawn.contains(player.getUUID()) && !isInFfaSpawn(player)){
            wasInSpawn.remove(player.getUUID());
            player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your kit was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        }
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
    public void finishSendToSpawn(NexiaPlayer player) {
        KitFFAPlayerData data = (KitFFAPlayerData) PlayerDataManager.getDataManager(NexiaCore.FFA_KITS_DATA_MANAGER).get(player);
        if(data.kit != null) data.kit.giveKit(player, true);
        else ServerTime.scheduler.schedule(() -> KitGUI.openKitGUI(player.unwrap()), 20);
    }

    @Override
    public void setInventory(NexiaPlayer player) {
        FfaKit ffaKit = ((KitFFAPlayerData) PlayerDataManager.getDataManager(NexiaCore.FFA_KITS_DATA_MANAGER).get(player)).kit;
        if(ffaKit != null) ffaKit.giveKit(player, false);
    }
}
