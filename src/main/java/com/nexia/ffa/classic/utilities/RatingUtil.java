package com.nexia.ffa.classic.utilities;

import com.combatreforged.factory.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.player.SavedPlayerData;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Collections;
import java.util.List;

import static com.nexia.ffa.classic.utilities.FfaAreas.ffaWorld;

public class RatingUtil {
    public static List<Score> leaderboardRating = null;
    static List<Score> oldLeaderboardRating = null;

    public static void calculateRating(ServerPlayer attacker, ServerPlayer player) {
        SavedPlayerData attackerData = PlayerDataManager.get(attacker).savedData;
        SavedPlayerData playerData = PlayerDataManager.get(player).savedData;

        double A = attackerData.elo;
        double B = playerData.elo;

        int killCount = KillTracker.getKillCount(attacker.getUUID(), player.getUUID());
        int victimKillCount = KillTracker.getKillCount(player.getUUID(), attacker.getUUID());
        double lr = (double) (victimKillCount + 1) / (victimKillCount + killCount + 2);

        double expected = 1 / (1 + Math.pow(10, (B - A) / 400));
        float health = FfaUtil.calculateHealth(attacker.getHealth());
        health = health / 10;
        double ratingChange = (int) (50 * (1 - expected)) * health * lr;

        double attackerNewRating = A + ratingChange;
        double victimNewRating = B - ratingChange;

        attackerData.elo = attackerNewRating;
        playerData.elo = victimNewRating;


        double expectedA = 1 / (1 + Math.pow(10, (0 - attackerNewRating) / 400));
        double expectedB = 1 / (1 + Math.pow(10, (0 - victimNewRating) / 400));

        attackerData.rating = expectedA / (1-expectedA);
        playerData.rating = expectedB / (1-expectedB);

        if (attacker.getServer() != null) {
            Scoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
            Objective ratingObjective = scoreboard.getObjective("Rating");
            if (ratingObjective == null) {
                ratingObjective = scoreboard.addObjective("Rating", ObjectiveCriteria.DUMMY, new TextComponent("Rating"), ObjectiveCriteria.RenderType.INTEGER);
            }
            scoreboard.getOrCreatePlayerScore(attacker.getScoreboardName(), ratingObjective).setScore((int) Math.round(attackerData.rating * 100));
            scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), ratingObjective).setScore((int) Math.round(playerData.rating * 100));
        }
    }

    public static void updateLeaderboard() {
        for (Entity entity : ffaWorld.getAllEntities()) {
            if (entity.getType() == EntityType.ARMOR_STAND) {
                entity.kill();
            }
        }

        String[] playerNames = new String[10];
        int[] scores = new int[10];

        Scoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
        Objective ratingObjective = scoreboard.getObjective("Rating");


        List<Score> playerScores = new java.util.ArrayList<>(scoreboard.getPlayerScores(ratingObjective).stream().toList());
        Collections.reverse(playerScores);
        leaderboardRating = playerScores;

        if (oldLeaderboardRating != null) removePlayersRank(oldLeaderboardRating);
        givePlayersRank(playerScores);

        oldLeaderboardRating = playerScores;

        int i = 0;
        for (Score score : playerScores) {
            if (i >= 10) break;
            playerNames[i] = score.getOwner();
            scores[i] = score.getScore();
            i++;
        }

        if (i < 10) {
            for (int j = i; j < 10; j++) {
                playerNames[j] = "N/A";
                scores[j] = 0;
            }
        }

        double x = 0.5;
        double y = 79.75;
        double z = -5.5;

        createArmorStand(ffaWorld, x, y + 1.25, z, ObjectMappings.convertComponent(MiniMessage.get().parse("<bold><gradient:#A201F9:#E401ED>LEADERBOARD</gradient></bold>")));
        createArmorStand(ffaWorld, x, y + 1, z, ObjectMappings.convertComponent(MiniMessage.get().parse("<bold><gradient:#A201F9:#E401ED>HIGHEST RATING</gradient></bold>")));
        createArmorStand(ffaWorld, x, y + 0.5, z, ObjectMappings.convertComponent(Component.text("#1 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[0]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[0]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y + 0.25, z, ObjectMappings.convertComponent(Component.text("#2 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[1]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[1]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y, z, ObjectMappings.convertComponent(Component.text("#3 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[2]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[2]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 0.25, z, ObjectMappings.convertComponent(Component.text("#4 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[3]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[3]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 0.5, z, ObjectMappings.convertComponent(Component.text("#5 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[4]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[4]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 0.75, z, ObjectMappings.convertComponent(Component.text("#6 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[5]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[5]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 1, z, ObjectMappings.convertComponent(Component.text("#7 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[6]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[6]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 1.25, z, ObjectMappings.convertComponent(Component.text("#8 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[7]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[7]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 1.5, z, ObjectMappings.convertComponent(Component.text("#9 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[8]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[8]).color(TextColor.fromHexString("#F1BA41"))))));
        createArmorStand(ffaWorld, x, y - 1.75, z, ObjectMappings.convertComponent(Component.text("#10 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[9]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[9]).color(TextColor.fromHexString("#F1BA41"))))));
    }

    private static void createArmorStand(ServerLevel ffaWorld, double x, double y, double z, net.minecraft.network.chat.Component customName) {
        ArmorStand armorStand = new ArmorStand(ffaWorld, x, y, z);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setCustomName(customName);
        armorStand.setCustomNameVisible(true);

        ffaWorld.addFreshEntity(armorStand);
    }

    public static double calculateRatingDifference(double newRating, double oldRating) {
        return (newRating - oldRating);
    }

    private static void givePlayersRank(List<Score> scores) {
        int i = 0;
        for (Score score : scores) {
            if (i >= 5) break;

            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayerByName(score.getOwner());
            if (player == null) {
                i++;
                continue;
            }

            if (Permissions.check(player, "nexia.rank")) {
                NexiaRank.addPrefix(NexiaRank.PRO, player, false);
            } else {
                NexiaRank.setRank(NexiaRank.PRO, player);
            }

            i++;
        }
    }

    private static void removePlayersRank(List<Score> oldScores) {
        int i = 0;
        for (Score oldScore : oldScores) {
            if (i >= 5) break;

            checkRatingRank(ServerTime.minecraftServer.getPlayerList().getPlayerByName(oldScore.getOwner()));

            i++;
        }
    }

    public static void checkRatingRank(ServerPlayer player) {
        if (RatingUtil.leaderboardRating == null || player == null) return;

        int i = 0;
        boolean isInTopFive = false;
        for (Score score : RatingUtil.leaderboardRating) {
            if (i >= 5) break;

            if (player.getScoreboardName().equals(score.getOwner())) {
                isInTopFive = true;
                break;
            }

            i++;
        }

        if (!isInTopFive) {
            if (Permissions.check(player, "nexia.rank")) {
                NexiaRank.removePrefix(NexiaRank.PRO, player);
            } else {
                NexiaRank.setRank(NexiaRank.DEFAULT, player);
            }
        } else {
            if (Permissions.check(player, "nexia.rank")) {
                NexiaRank.addPrefix(NexiaRank.PRO, player, false);
            } else {
                NexiaRank.setRank(NexiaRank.PRO, player);
            }
        }
    }
}