package com.nexia.minigames.games.duels.custom;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class CustomDuelsGame extends DuelsGame {

    public CustomDuelsGame(NexiaPlayer p1, NexiaPlayer p2, DuelsMap map, ServerLevel level, int endTime, int startTime){
        super(p1, p2, null, map, level, endTime, startTime);
    }

    public static CustomDuelsGame startGame(NexiaPlayer p1, NexiaPlayer p2, String kitID, @Nullable DuelsMap selectedMap){

        String perCustomKitID = null;

        if(!DuelGameHandler.validCustomKit(p1, kitID)){
            NexiaCore.logger.error(String.format("[Nexia]: Invalid custom duel kit (%s) selected!", kitID));
            kitID = "";
        }

        DuelsPlayerData invitorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(p1);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(p2);

        if (invitorData.inviteOptions.perCustomDuel && !DuelGameHandler.validCustomKit(p1, invitorData.inviteOptions.inviteKit2)) {
            NexiaCore.logger.error(String.format("[Nexia]: Invalid per-custom (2) duel kit (%s) selected!", invitorData.inviteOptions.inviteKit2));
        } else {
            perCustomKitID = invitorData.inviteOptions.inviteKit2;
        }

        if(invitorData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(p1, invitorData.duelOptions.spectatingPlayer, false);
        }
        if(playerData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(p2, playerData.duelOptions.spectatingPlayer, false);
        }

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), true);
        if(selectedMap == null){
            selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
        }

        selectedMap.structureMap.pasteMap(duelLevel);

        selectedMap.p1Pos.teleportPlayer(duelLevel, p1);
        selectedMap.p2Pos.teleportPlayer(duelLevel, p2);

        p1.reset(true, Minecraft.GameMode.ADVENTURE);
        p2.reset(true, Minecraft.GameMode.ADVENTURE);

        playerData.inviteOptions.reset();
        playerData.inDuel = true;
        playerData.duelOptions.spectatingPlayer = null;

        invitorData.inDuel = true;
        invitorData.duelOptions.spectatingPlayer = null;

        removeQueue(p1, null, true);
        removeQueue(p2, null, true);


        p2.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p1.getRawName()).color(ChatFormat.brandColor2))));

        p1.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p2.getRawName())).color(ChatFormat.brandColor2)));

        File p1File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + p1.getUUID(), kitID.toLowerCase() + ".txt");
        if (p1File.exists()) {
            InventoryUtil.loadInventory(p1, "duels/custom/" + p1.getUUID(), kitID.toLowerCase());

            if (perCustomKitID != null && !perCustomKitID.trim().isEmpty()) {
                File p2File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + p2.getUUID(), perCustomKitID.toLowerCase() + ".txt");
                if (p2File.exists()) InventoryUtil.loadInventory(p2, "duels/custom/" + p2.getUUID(), perCustomKitID.toLowerCase());
                else InventoryUtil.loadInventory(p2, "duels/custom/" + p1.getUUID(), kitID.toLowerCase());
            } else InventoryUtil.loadInventory(p2, "duels/custom/" + p1.getUUID(), kitID.toLowerCase());
        } else {
            InventoryUtil.loadInventory(p1, "duels", "classic");
            InventoryUtil.loadInventory(p2, "duels", "classic");
        }

        invitorData.inviteOptions.reset();

        playerData.gameMode = DuelGameMode.CLASSIC;
        invitorData.gameMode = DuelGameMode.CLASSIC;

        CustomDuelsGame game = new CustomDuelsGame(p1, p2, selectedMap, duelLevel, 5, 5);

        playerData.gameOptions = new DuelOptions.GameOptions(game, p1);
        invitorData.gameOptions = new DuelOptions.GameOptions(game, p2);

        DuelGameHandler.duelsGames.add(game);

        game.uuid = gameUUID;

        return game;
    }
}