package com.nexia.minigames.games.duels.custom.team;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class CustomTeamDuelsGame extends TeamDuelsGame {
    public boolean perCustomDuel;

    public CustomTeamDuelsGame(DuelsTeam team1, DuelsTeam team2, DuelsMap map, ServerLevel level, int endTime, int startTime) {
        super(team1, team2, null, map, level, endTime, startTime);
    }

    public static CustomTeamDuelsGame startGame(@NotNull DuelsTeam team1, @NotNull DuelsTeam team2, String kitID, @Nullable DuelsMap selectedMap) {
        String perCustomKitID = null;

        if(!DuelGameHandler.validCustomKit(team1.getLeader(), kitID)){
            NexiaCore.logger.error(String.format("[Nexia]: Invalid custom duel kit (%s) selected!", kitID));
            kitID = "";
        }

        DuelsPlayerData team1LeaderData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(team1.getLeader());
        if(team1LeaderData.inviteOptions.perCustomDuel && !DuelGameHandler.validCustomKit(team2.getLeader(), team1LeaderData.inviteOptions.inviteKit2))
            NexiaCore.logger.error(String.format("[Nexia]: Invalid per-custom (team 2) duel kit (%s) selected!", team1LeaderData.inviteOptions.inviteKit2));
        else perCustomKitID = team1LeaderData.inviteOptions.inviteKit2;

        team1.alive.clear();
        team1.alive.addAll(team1.all);

        team2.alive.clear();
        team2.alive.addAll(team2.all);

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), true);
        if (selectedMap == null) selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));

        selectedMap.structureMap.pasteMap(duelLevel);

        File kitFile = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + team1.getLeader().getUUID(), kitID.toLowerCase() + ".txt");
        File p2File = null;

        if(perCustomKitID != null && !perCustomKitID.trim().isEmpty()) p2File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + team2.getLeader().getUUID(), perCustomKitID.toLowerCase() + ".txt");

        CustomTeamDuelsGame game = new CustomTeamDuelsGame(team1, team2, selectedMap, duelLevel, 5, 5);

        if(p2File != null && p2File.exists()) game.perCustomDuel = true;

        DuelGameHandler.teamDuelsGames.add(game);

        for (NexiaPlayer player : team1.all) {
            ServerPlayer serverPlayer = player.unwrap();
            DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

            data.gameMode = DuelGameMode.CLASSIC;
            data.gameOptions = new DuelOptions.GameOptions(game, team2);
            data.inviteOptions.reset();
            data.duelOptions.spectatingPlayer = null;
            data.inDuel = true;

            removeQueue(player, null, true);

            selectedMap.p1Pos.teleportPlayer(duelLevel, serverPlayer);

            player.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                            .append(Component.text(team2.getLeader().getRawName() + "'s Team")
                                    .color(ChatFormat.brandColor2))));

            if(kitFile.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + team1.getLeader().getUUID(), kitID.toLowerCase());
            else InventoryUtil.loadInventory(player, "duels", "classic");

            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            player.reset(true, Minecraft.GameMode.ADVENTURE);
        }

        for (NexiaPlayer player : team2.all) {
            ServerPlayer serverPlayer = player.unwrap();
            DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

            data.gameMode = DuelGameMode.CLASSIC;
            data.gameOptions = new DuelOptions.GameOptions(game, team1);
            data.inviteOptions.reset();
            data.duelOptions.spectatingPlayer = null;
            data.inDuel = true;

            removeQueue(player, null, true);

            selectedMap.p2Pos.teleportPlayer(duelLevel, serverPlayer);

            player.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                            .append(Component.text(team1.getLeader().getRawName() + "'s Team")
                                    .color(ChatFormat.brandColor2))));


            if(game.perCustomDuel) {
                if(p2File != null && p2File.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + team2.getLeader().getUUID(), perCustomKitID.toLowerCase());
                else InventoryUtil.loadInventory(player, "duels", "classic");
            } else {
                if(kitFile.exists()) InventoryUtil.loadInventory(player, "duels/custom/" + team1.getLeader().getUUID(), kitID.toLowerCase());
                else InventoryUtil.loadInventory(player, "duels", "classic");
            }

            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

           player.reset(true, Minecraft.GameMode.ADVENTURE);
        }

        game.uuid = gameUUID;

        return game;
    }
}