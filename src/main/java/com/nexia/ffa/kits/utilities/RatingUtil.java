package com.nexia.ffa.kits.utilities;

import com.combatreforged.factory.api.world.World;
import com.combatreforged.factory.api.world.entity.Entity;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.combatreforged.factory.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import com.nexia.ffa.kits.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.nexia.core.utilities.time.ServerTime.*;

public class RatingUtil {
    static ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("ffa", "kits"), new RuntimeWorldConfig()).asWorld();
    static List<Score> oldScores = null;

    public static void calculateRating(ServerPlayer attacker, ServerPlayer player) {
        SavedPlayerData attackerData = PlayerDataManager.get(attacker).savedData;
        SavedPlayerData playerData = PlayerDataManager.get(player).savedData;

        int killCount = KillTracker.getKillCount(attacker.getUUID(), player.getUUID());
        int victimKillCount = KillTracker.getKillCount(player.getUUID(), attacker.getUUID());

        double attackerOldRating = attackerData.rating;
        double victimOldRating = playerData.rating;
        double killWeight = Math.sqrt(victimOldRating / attackerOldRating) + Math.sqrt((double) Math.max(1, victimKillCount) / Math.max(1, killCount));
        double deathWeight = 1 / Math.sqrt(attackerOldRating / victimOldRating) + 1 / Math.sqrt((double) Math.max(1, killCount) / Math.max(1, victimKillCount));
        killWeight = killWeight * (Math.max(1, attackerData.relative_increase) + Math.max(1, attackerData.relative_decrease)) / 40;
        deathWeight = deathWeight * (Math.max(1, playerData.relative_increase) + Math.max(1, playerData.relative_decrease)) / 40;
        double attackerRelativeIncrease = attackerData.relative_increase + killWeight;
        double attackerRelativeDecrease = attackerData.relative_decrease;
        double victimRelativeIncrease = playerData.relative_increase;
        double victimRelativeDecrease = playerData.relative_decrease + deathWeight;

        attackerData.relative_increase = attackerRelativeIncrease;
        attackerData.relative_decrease = attackerRelativeDecrease;
        playerData.relative_increase = victimRelativeIncrease;
        playerData.relative_decrease = victimRelativeDecrease;

        double attackerNewRating = (attackerRelativeIncrease) / (attackerRelativeDecrease);
        double victimNewRating = (victimRelativeIncrease) / (victimRelativeDecrease);

        attackerData.rating = attackerNewRating;
        playerData.rating = victimNewRating;

        if (attacker.getServer() != null) {
            Scoreboard scoreboard = attacker.getServer().getScoreboard();
            Objective ratingObjective = scoreboard.getObjective("Rating");
            if (ratingObjective == null) {
                ratingObjective = scoreboard.addObjective("Rating", ObjectiveCriteria.DUMMY, new TextComponent("Rating"), ObjectiveCriteria.RenderType.INTEGER);
            }
            scoreboard.getOrCreatePlayerScore(attacker.getScoreboardName(), ratingObjective).setScore((int) Math.round(attackerNewRating * 100));
            scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), ratingObjective).setScore((int) Math.round(victimNewRating * 100));
        }

    }

    public static void updateLeaderboard() {
        if (!factoryServer.getPlayers().isEmpty()) {
            Player player = (Player) factoryServer.getPlayers().toArray()[0];
            World world = player.getWorld();

            for (Entity entity : world.getEntities()) {
                if (entity.getEntityType() == Minecraft.Entity.ARMOR_STAND) {
                    entity.kill();
                }
            }

            String[] playerNames = new String[10];
            int[] scores = new int[10];

            Scoreboard scoreboard = minecraftServer.getScoreboard();
            Objective ratingObjective = scoreboard.getObjective("Rating");


            List<Score> playerScores = new java.util.ArrayList<>(scoreboard.getPlayerScores(ratingObjective).stream().toList());
            Collections.reverse(playerScores);

            givePlayersProRank(playerScores, oldScores);

            oldScores = playerScores;

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

            createArmorStand(level, x, y + 1.25, z, ObjectMappings.convertComponent(Component.text("LEADERBOARD").color(TextColor.fromHexString("#A201F9")).decorate(TextDecoration.BOLD)));
            createArmorStand(level, x, y + 1, z, ObjectMappings.convertComponent(Component.text("HIGHEST RATING").color(TextColor.fromHexString("#A201F9")).decorate(TextDecoration.BOLD)));
            createArmorStand(level, x, y + 0.5, z, ObjectMappings.convertComponent(Component.text("#1 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[0]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[0]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y + 0.25, z, ObjectMappings.convertComponent(Component.text("#2 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[1]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[1]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y, z, ObjectMappings.convertComponent(Component.text("#3 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[2]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[2]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 0.25, z, ObjectMappings.convertComponent(Component.text("#4 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[3]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[3]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 0.5, z, ObjectMappings.convertComponent(Component.text("#5 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[4]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[4]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 0.75, z, ObjectMappings.convertComponent(Component.text("#6 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[5]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[5]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 1, z, ObjectMappings.convertComponent(Component.text("#7 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[6]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[6]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 1.25, z, ObjectMappings.convertComponent(Component.text("#8 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[7]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[7]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 1.5, z, ObjectMappings.convertComponent(Component.text("#9 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[8]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[8]).color(TextColor.fromHexString("#F1BA41"))))));
            createArmorStand(level, x, y - 1.75, z, ObjectMappings.convertComponent(Component.text("#10 ").color(TextColor.fromHexString("#E401ED")).append(Component.text(playerNames[9]).append(Component.text(" » ").color(NamedTextColor.WHITE)).append(Component.text(scores[9]).color(TextColor.fromHexString("#F1BA41"))))));

        }
    }

    private static void createArmorStand(ServerLevel level, double x, double y, double z, net.minecraft.network.chat.Component customName) {
        ArmorStand armorStand = new ArmorStand(level, x, y, z);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setCustomName(customName);
        armorStand.setCustomNameVisible(true);

        level.addFreshEntity(armorStand);
    }

    public static double calculateRatingDifference(double newRating, double oldRating) {
        return (newRating - oldRating) * 100;
    }

    private static void givePlayersProRank(List<Score> newScores, List<Score> oldScores) {
        if (oldScores == null) return;
        List<String> playerList = new ArrayList<>();

        int i = 0;
        for (Score score : newScores) {
            if (i >= 5) break;
            playerList.add(score.getOwner());

            // TODO give rank

            i += 1;
        }

        int j = 0;
        for (Score score : oldScores) {
            if (j >= 5) break;
            if (!playerList.contains(score.getOwner())) {
                // TODO remove rank
            }
            j += 1;
        }
    }
}