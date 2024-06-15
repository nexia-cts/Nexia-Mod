package com.nexia.minigames.games.bedwars;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
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
import com.nexia.minigames.games.bedwars.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;

import java.time.Duration;
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

    public static ArrayList<NexiaPlayer> queueList = new ArrayList<>();
    public static final int requiredPlayers = playersInTeam * 2;

    public static final int queueWaitTime = 15;
    private static final List<Integer> announcedQueueSeconds = Arrays.asList(15, 10, 5, 4, 3, 2, 1);
    public static int queueCountdown = queueWaitTime;
    public static boolean isQueueCountdownActive = false;

    public static boolean winScreen = false;
    protected static final int winScreenTime = 7;
    protected static int winScreenTimer = winScreenTime;

    public static ArrayList<NexiaPlayer> spectatorList = new ArrayList<>();
    public static PlayerTeam spectatorTeam;

    public static HashMap<NexiaPlayer, Integer> respawningList = new HashMap<>();
    public static final int respawnTime = 5;

    public static HashMap<NexiaPlayer, Integer> invulnerabilityList = new HashMap<>();
    public static final int invulnerabilityTime = 1;
    public static HashMap<NexiaPlayer, ItemStack[]> invisiblePlayerArmor = new HashMap<>();
    public static HashMap<NexiaPlayer, ArrayList<BwTrident>> gameTridents = new HashMap<>();

    public static ArrayList<NexiaPlayer> winners = new ArrayList<>();
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
        for (Iterator<NexiaPlayer> it = respawningList.keySet().iterator(); it.hasNext(); ) {
            NexiaPlayer player = it.next();
            int timeLeft = respawningList.get(player);

            if (timeLeft % 20 == 0) {
                player.sendTitle(Title.title(Component.text("You died!").color(ChatFormat.Minecraft.red),
                        Component.text("Respawning in ").color(ChatFormat.Minecraft.gray)
                                .append(Component.text(timeLeft / 20).color(ChatFormat.brandColor2))
                                .append(Component.text("...").color(ChatFormat.Minecraft.gray)),
                        Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0))
                ));

            }
            respawningList.replace(player, timeLeft - 1);
            if (respawningList.get(player) <= 0) {
                it.remove();
                if (BwUtil.isBedWarsPlayer(player)) BwPlayers.sendToSpawn(player);
            }
        }
    }

    private static void invulnerabilityTick() {
        for (Iterator<NexiaPlayer> it = invulnerabilityList.keySet().iterator(); it.hasNext(); ) {
            NexiaPlayer player = it.next();
            invulnerabilityList.replace(player, invulnerabilityList.get(player) - 1);
            if (invulnerabilityList.get(player) <= 0) {
                it.remove();
                if (BwUtil.isBedWarsPlayer(player)) player.unwrap().setInvulnerable(false);
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
            for(NexiaPlayer player : queueList) {
                player.sendMessage(Component.text("The game will start in ").color(ChatFormat.Minecraft.gray)
                        .append(Component.text(queueCountdown).color(ChatFormat.brandColor2)
                                .append(Component.text( " seconds.").color(ChatFormat.Minecraft.gray)))
                );
            }
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

        if (secondsLeft <= gameEndWarningTimes.getFirst()) {
            for (Integer warningTime : gameEndWarningTimes) {
                if (secondsLeft == warningTime) {
                    for(NexiaPlayer player : BwPlayers.getViewers()) {
                        player.sendMessage(Component.text("The game will end in ").color(ChatFormat.Minecraft.gray)
                                .append(Component.text(secondsLeft).color(ChatFormat.Minecraft.red))
                                .append(Component.text(" seconds.").color(ChatFormat.Minecraft.gray))
                        );
                    }

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

            for (NexiaPlayer player : team.players) {
                player.unwrap().getEnderChestInventory().clearContent();
                player.getInventory().clear();
                BwPlayers.sendToSpawn(player);

                player.sendMessage(Component.text("The game has started. ", ChatFormat.Minecraft.gray)
                        .append(Component.text("Good luck!", ChatFormat.brandColor2))
                );

                player.sendTitle(Title.title(
                        Component.text("You're ", ChatFormat.normalColor)
                                .append(Component.text(team.textColor + team.displayName))
                                .append(Component.text("!", ChatFormat.normalColor)),


                        Component.text(""),
                        Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(3), Duration.ofSeconds(1))
                ));

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
            winners = aliveTeams.getFirst().players;
            winnerColor = aliveTeams.getFirst().armorColor;
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

        Component victoryTitle = Component.text("Victory!", ChatFormat.goldColor);
        Component gameEndTitle = Component.text("Game over!", ChatFormat.goldColor);
        Title.Times time = Title.Times.of(Duration.ofMillis(250), Duration.ofSeconds(4), Duration.ofSeconds(1));

        if (aliveTeams.size() == 1) {
            BwTeam winnerTeam = aliveTeams.getFirst();
            String whoWon;
            if (winnerTeam.players.size() == 1) {
                PlayerDataManager.get(winnerTeam.players.stream().findFirst().get()).savedData.wins++;
                whoWon = winnerTeam.textColor + winnerTeam.players.getFirst().getRawName();
            } else {
                whoWon = winnerTeam.textColor + winnerTeam.displayName + " team";
                for(NexiaPlayer player : winnerTeam.players) {
                    PlayerDataManager.get(player).savedData.wins++;
                }
            }

            Component subtitle = Component.text(whoWon).append(Component.text(" has won the game!", ChatFormat.Minecraft.gray));

            for(NexiaPlayer player : winnerTeam.players) {
                player.sendTitle(Title.title(victoryTitle, subtitle, time));
            }

            for(NexiaPlayer player : spectatorList) {
                player.sendTitle(Title.title(gameEndTitle, subtitle, time));
            }

            for(NexiaPlayer player : BwPlayers.getViewers()) {
                player.sendMessage(subtitle);
            }

        } else {
            Component subtitle = Component.text("The game ended in a ", ChatFormat.Minecraft.gray)
                    .append(Component.text("draw", ChatFormat.goldColor))
                    .append(Component.text("!", ChatFormat.Minecraft.gray));


            for(NexiaPlayer player : BwPlayers.getViewers()) {
                player.sendTitle(Title.title(gameEndTitle, subtitle, time));
                player.sendMessage(subtitle);
            }
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

        for (NexiaPlayer player : gameTridents.keySet()) {
            for (BwTrident trident : gameTridents.get(player)) {
                trident.remove();
            }
        }
        gameTridents.clear();
    }

    private static void resetBedWars() {
        isGameActive = false;

        BwAreas.spawnQueueBuild();

        try {
            for (NexiaPlayer player : spectatorList) {
                if (player == null || player.unwrap() == null) continue;
                LobbyUtil.returnToLobby(player, true);
                BwScoreboard.removeScoreboardFor(player);
            }
        } catch (Exception ignored) { }

        spectatorList.clear();
    }

}
