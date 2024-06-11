package com.nexia.ffa.classic.utilities;

import com.combatreforged.factory.api.world.World;
import com.combatreforged.factory.api.world.entity.Entity;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.combatreforged.factory.builder.implementation.util.ObjectMappings;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.classic.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.player.SavedPlayerData;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
import java.util.Collections;
import java.util.List;

import static com.nexia.core.utilities.time.ServerTime.*;

public class RatingUtil {
    static ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("ffa", "classic"), new RuntimeWorldConfig()).asWorld();
    static List<Player> oldPlayerList = new ArrayList<>();

    public static void calculateRating(ServerPlayer attacker, ServerPlayer player) {
        SavedPlayerData attackerData = PlayerDataManager.get(attacker).savedData;
        SavedPlayerData playerData = PlayerDataManager.get(player).savedData;

        double A = attackerData.elo;
        double B = playerData.elo;

        int killCount = KillTracker.getKillCount(attacker.getUUID(), player.getUUID());
        int victimKillCount = KillTracker.getKillCount(player.getUUID(), attacker.getUUID());
        int total = killCount + victimKillCount;

        double expected = 1 / (1 + Math.pow(10, (B - A) / 400));

        double ratingChange = (int) (200 * (1 - expected)) / (10 + total);

        double attackerNewRating = A + ratingChange;
        double victimNewRating = B - ratingChange;

        attackerData.elo = attackerNewRating;
        playerData.elo = victimNewRating;

        double expectedA = 1 / (1 + Math.pow(10, (0 - attackerNewRating) / 400));
        double expectedB = 1 / (1 + Math.pow(10, (0 - victimNewRating) / 400));

        attackerData.rating = expectedA / (1-expectedA);
        playerData.rating = expectedB / (1-expectedB);



        if (attacker.getServer() != null) {
            Scoreboard scoreboard = attacker.getServer().getScoreboard();
            Objective ratingObjective = scoreboard.getObjective("Rating");
            if (ratingObjective == null) {
                ratingObjective = scoreboard.addObjective("Rating", ObjectiveCriteria.DUMMY, new TextComponent("Rating"), ObjectiveCriteria.RenderType.INTEGER);
            }
            scoreboard.getOrCreatePlayerScore(attacker.getScoreboardName(), ratingObjective).setScore((int) Math.round(attackerData.rating * 100));
            scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), ratingObjective).setScore((int) Math.round(playerData.rating * 100));
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

            givePlayersRank(playerScores);

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
        return (newRating - oldRating);
    }

    private static void givePlayersRank(List<Score> scores) {
        List<Player> playerList = new ArrayList<>();

        int i = 0;
        for (Score score : scores) {
            if (i >= 5) break;

            Player player = factoryServer.getPlayer(score.getOwner());
            if (player instanceof ServerPlayer serverPlayer) {
                if (Permissions.check(serverPlayer, "nexia.prefix.pro")) {
                    if (Permissions.check(serverPlayer, "nexia.rank")) {
                        factoryServer.runCommand("/staffprefix set " + serverPlayer.getScoreboardName() + " default");
                        factoryServer.runCommand("/staffprefix remove " + serverPlayer.getScoreboardName() + " pro");
                    }
                    factoryServer.runCommand("/rank " + serverPlayer.getScoreboardName() + " default", 4, false);
                }

                if (!Permissions.check(serverPlayer, "nexia.prefix.pro")) {
                    factoryServer.runCommand("/staffprefix add " + serverPlayer.getScoreboardName() + " pro", 4, false);
                }
            }

            i += 1;
        }

        if (!oldPlayerList.isEmpty()) {
            for (Player player : oldPlayerList) {
                if (playerList.contains(player)) continue;

                if (player instanceof ServerPlayer serverPlayer) {
                    if (Permissions.check(serverPlayer, "nexia.prefix.pro")) {
                        if (Permissions.check(serverPlayer, "nexia.rank")) {
                            factoryServer.runCommand("/staffprefix set " + serverPlayer.getScoreboardName() + " default");
                            factoryServer.runCommand("/staffprefix remove " + serverPlayer.getScoreboardName() + " pro");
                        }
                        factoryServer.runCommand("/rank " + serverPlayer.getScoreboardName() + " default", 4, false);
                    }

                    if (!Permissions.check(serverPlayer, "nexia.prefix.pro")) {
                        factoryServer.runCommand("/staffprefix remove " + serverPlayer.getScoreboardName() + " pro", 4, false);
                    }
                }
            }
            oldPlayerList = playerList;
        } else {
            oldPlayerList = playerList;
        }
    }
}