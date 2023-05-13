package com.nexia.minigames.games.bedwars;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.custom.BwTrident;
import com.nexia.minigames.games.bedwars.players.BwPlayers;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import com.nexia.minigames.games.bedwars.upgrades.BwApplyTraps;
import com.nexia.minigames.games.bedwars.upgrades.BwApplyUpgrades;
import com.nexia.minigames.games.bedwars.upgrades.BwTrap;
import com.nexia.minigames.games.bedwars.util.BwGen;
import com.nexia.minigames.games.bedwars.util.BwPlayerTracker;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;

import java.util.*;
import java.util.function.Predicate;

public class BwGame {

    public static final String bedWarsDirectory = NxFileUtil.addConfigDir("bedwars");

    public static boolean isGameActive = false;
    public static int gameTicks = 0;
    public static final int gameLength = 30 * 60 + 1;
    private static final List<Integer> gameEndWarningTimes = Arrays.asList(60, 30, 15, 10, 5, 4, 3, 2, 1);

    public static final int playersInTeam = 1;
    public static int maxPlayerCount = 0;

    public static ArrayList<ServerPlayer> queueList = new ArrayList<>();
    public static final int requiredPlayers = playersInTeam * 2;

    public static final int queueWaitTime = 15;
    private static final List<Integer> announcedQueueSeconds = Arrays.asList(15, 10, 5, 4, 3, 2, 1);
    public static int queueCountdown = queueWaitTime;
    public static boolean isQueueCountdownActive = false;

    public static boolean winScreen = false;
    protected static final int winScreenTime = 7;
    protected static int winScreenTimer = winScreenTime;

    public static ArrayList<ServerPlayer> spectatorList = new ArrayList<>();
    public static PlayerTeam spectatorTeam;

    public static HashMap<ServerPlayer, Integer> respawningList = new HashMap<>();
    public static final int respawnTime = 5;

    public static HashMap<ServerPlayer, Integer> invulnerabilityList = new HashMap<>();
    public static final int invulnerabilityTime = 1;
    public static HashMap<ServerPlayer, ItemStack[]> invisiblePlayerArmor = new HashMap<>();
    public static HashMap<ServerPlayer, ArrayList<BwTrident>> gameTridents = new HashMap<>();

    public static ArrayList<ServerPlayer> winners = new ArrayList<>();
    public static Integer winnerColor = null;

    // ----- TICK METHODS --------------------------------------------------------------------------------

    public static void firstTick() {
        BwAreas.setBedWarsWorld(ServerTime.minecraftServer);
        BwAreas.spawnQueueBuild();
        BwTeam.resetTeams();
        BwUtil.createSpectatorTeam();
    }

    public static void tick() {
        gameTicks++;
        BwGen.genTick();
        BwUtil.utilTick();
        BwAreas.tick();
        invulnerabilityTick();
        respawnTick();

        if (gameTicks == 1) tickAfterStart();
    }

    private static void respawnTick() {
        for (Iterator<ServerPlayer> it = respawningList.keySet().iterator(); it.hasNext(); ) {
            ServerPlayer player = it.next();
            int timeLeft = respawningList.get(player);

            if (timeLeft % 20 == 0) {
                PlayerUtil.sendTitle(player, "\247cYou died!",
                        LegacyChatFormat.brandColor1 + "Respawning in " +
                                LegacyChatFormat.brandColor2 + timeLeft / 20 + LegacyChatFormat.brandColor1 + "...",
                        0, 20, 10);
            }
            respawningList.replace(player, timeLeft - 1);
            if (respawningList.get(player) <= 0) {
                it.remove();
                if (BwUtil.isBedWarsPlayer(player)) BwPlayers.sendToSpawn(PlayerUtil.getFixedPlayer(player));
            }
        }
    }

    private static void invulnerabilityTick() {
        for (Iterator<ServerPlayer> it = invulnerabilityList.keySet().iterator(); it.hasNext(); ) {
            ServerPlayer player = it.next();
            invulnerabilityList.replace(player, invulnerabilityList.get(player) - 1);
            if (invulnerabilityList.get(player) <= 0) {
                it.remove();
                if (BwUtil.isBedWarsPlayer(player)) player.setInvulnerable(false);
            }
        }
    }

    // ----- SECOND METHODS --------------------------------------------------------------------------------

    public static void bedWarsSecond() {
        if (isQueueCountdownActive) queueSecond();
        if (winScreen) winScreenSecond();

        if (isGameActive && !winScreen) {
            BwPlayerTracker.trackerSecond();
            BwApplyUpgrades.upgradeSecond();
            BwApplyTraps.trapSecond();
            timerSecond();
        }
    }

    private static void queueSecond() {
        if (queueCountdown <= 0) {
            isQueueCountdownActive = false;
            queueCountdown = queueWaitTime;
            startBedWars();
        } else if (announcedQueueSeconds.contains(queueCountdown)) {
            PlayerUtil.broadcast(queueList, LegacyChatFormat.chatColor2 + "The game will start in " +
                    LegacyChatFormat.brandColor2 + queueCountdown + LegacyChatFormat.chatColor2 + " seconds.");
        }
        if (queueCountdown > 0 && queueCountdown <= 3) {
            PlayerUtil.broadcastSound(queueList, SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, 0.05f, 1f);
        }
        queueCountdown--;

    }

    private static void winScreenSecond() {

        if (winScreenTimer <= winScreenTime && winScreenTimer >= 3) {
            BwTeam.winnerRockets(winners, winnerColor);
        }

        if (winScreenTimer <= 0) {
            winScreen = false;
            winScreenTimer = winScreenTime;
            resetBedWars();
        }

        winScreenTimer--;
    }

    private static void timerSecond() {
        BwScoreboard.updateTimer();

        int secondsLeft = gameLength - gameTicks / 20;

        if (secondsLeft <= gameEndWarningTimes.get(0)) {
            for (Integer warningTime : gameEndWarningTimes) {
                if (secondsLeft == warningTime) {
                    PlayerUtil.broadcast(BwPlayers.getViewers(),
                            "\2477The game will end in \247c" + secondsLeft + " \2477seconds.");
                    break;
                }
            }
        }

        if (secondsLeft <= 0) {
            endBedwars();
        }
    }

    // ----- START & END --------------------------------------------------------------------------------

    public static void startQueueCountdown() {
        queueCountdown = queueWaitTime;
        isQueueCountdownActive = true;
    }

    public static void endQueueCountdown() {
        queueCountdown = queueWaitTime;
        isQueueCountdownActive = false;
    }

    public static void startBedWars() {
        gameTicks = 0;

        BwAreas.clearBedWarsMap();
        BwAreas.clearQueueBuild();
        clearGameEntities();

        BwTeam.spreadIntoTeams(queueList);
        BwTeam.setSpawns();
        BwTeam.createBeds();
        BwGen.createGens();
        BwTrap.resetTrapLocations();
        BwScoreboard.setUpScoreboard();
        preparePlayers();

        isGameActive = true;
    }

    private static void preparePlayers() {
        for (BwTeam team : BwTeam.allTeams.values()) {
            if (team.players == null) continue;

            for (ServerPlayer player : team.players) {
                player.getEnderChestInventory().clearContent();
                player.inventory.clearContent();
                BwPlayers.sendToSpawn(player);
                player.sendMessage(new TextComponent(LegacyChatFormat.chatColor2 + "The game has started. " +
                        LegacyChatFormat.brandColor2 + "Good luck" + LegacyChatFormat.chatColor2 + "!"), Util.NIL_UUID);
                PlayerUtil.sendTitle(player,
                        "You're " + team.textColor + team.displayName + "\247f!", "", 0, 60, 20);
                player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            }
        }
    }

    private static void tickAfterStart() {
        PlayerUtil.broadcastSound(BwPlayers.getPlayers(),
                SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, 0.05f, 2f);
    }

    public static void endBedwars() {
        winScreen = true;

        ArrayList<BwTeam> aliveTeams = BwTeam.getAliveTeams();
        if (aliveTeams.size() == 1) {
            winners = aliveTeams.get(0).players;
            winnerColor = aliveTeams.get(0).armorColor;
        } else {
            winners = new ArrayList<>();
            winnerColor = null;
        }

        sendGameEndMessage();

        invisiblePlayerArmor.clear();
        spectatorList.addAll(BwPlayers.getPlayers());
        respawningList.clear();
        clearGameEntities();
        BwTeam.resetTeams();
        BwGen.resetGens();
    }

    private static void sendGameEndMessage() {
        ArrayList<BwTeam> aliveTeams = BwTeam.getAliveTeams();

        String victoryTitle = "\2476Victory!";
        String gameEndTitle = "\2476Game Over!";
        int[] time = {5, 80, 20};

        if (aliveTeams.size() == 1) {
            BwTeam winnerTeam = aliveTeams.get(0);
            String whoWon;
            if (winnerTeam.players.size() == 1) {
                whoWon = winnerTeam.textColor + winnerTeam.players.get(0).getScoreboardName();
            } else {
                whoWon = winnerTeam.textColor + winnerTeam.displayName + " team";
            }

            String subtitle = whoWon + "\2477 has won the game!";
            PlayerUtil.broadcastTitle(winnerTeam.players, victoryTitle, subtitle, time[0], time[1], time[2]);
            PlayerUtil.broadcastTitle(spectatorList, gameEndTitle, subtitle, time[0], time[1], time[2]);
            PlayerUtil.broadcast(BwPlayers.getViewers(), subtitle);

        } else {
            String subtitle = "\2477The game ended in a draw!";
            PlayerUtil.broadcastTitle(BwPlayers.getViewers(), gameEndTitle, subtitle, time[0], time[1], time[2]);
            PlayerUtil.broadcast(BwPlayers.getViewers(), subtitle);
        }
    }

    private static void clearGameEntities() {
        BlockVec3 corner1 = new BlockVec3(BwAreas.bedWarsCorner1);
        corner1.y = 0;
        BlockVec3 corner2 = new BlockVec3(BwAreas.bedWarsCorner2);
        corner2.y = 300;
        AABB aabb = new AABB(corner1.toBlockPos(), corner2.toBlockPos());
        Predicate<Entity> predicate = o -> true;
        for (Entity entity : BwAreas.bedWarsWorld.getEntities(EntityType.ITEM, aabb, predicate)) {
            entity.remove();
        }
        entityLoop: for (Entity entity : BwAreas.bedWarsWorld.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            for (String tag : entity.getTags()) {
                if (tag.equals(BwGen.genTimerDisplayTag) || tag.equals(BwGen.genItemDisplayTag)) {
                    entity.remove();
                    continue entityLoop;
                }
            }
        }

        for (ServerPlayer player : gameTridents.keySet()) {
            for (BwTrident trident : gameTridents.get(player)) {
                trident.remove();
            }
        }
        gameTridents.clear();
    }

    private static void resetBedWars() {
        isGameActive = false;

        BwAreas.spawnQueueBuild();

        for (ServerPlayer player : spectatorList) {
            player = PlayerUtil.getFixedPlayer(player);
            if (player == null) continue;
            LobbyUtil.returnToLobby(player, true);
            BwScoreboard.removeScoreboardFor(player);
        }
        spectatorList.clear();
    }

}
