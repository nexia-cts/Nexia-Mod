package com.nexia.ffa.kits.utilities;

import com.combatreforged.factory.api.world.World;
import com.combatreforged.factory.api.world.entity.Entity;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import com.nexia.ffa.kits.utilities.player.SavedPlayerData;
import net.minecraft.network.chat.Component;
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

import static com.nexia.core.utilities.time.ServerTime.factoryServer;
import static com.nexia.core.utilities.time.ServerTime.minecraftServer;

public class RatingUtil {
    static ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("ffa", "kits"), new RuntimeWorldConfig()).asWorld();

    public static double[] calculateRating(ServerPlayer attacker, ServerPlayer player) {
        SavedPlayerData attackerData = PlayerDataManager.get(attacker).savedData;
        SavedPlayerData playerData = PlayerDataManager.get(player).savedData;

        int killCount = KillTracker.getKillCount(attacker.getUUID(), player.getUUID());
        int victimKillCount = KillTracker.getKillCount(player.getUUID(), attacker.getUUID());

        double attackerOldRating = attackerData.rating;
        double victimOldRating = playerData.rating;

        double attackerRelativeIncrease = attackerData.relative_increase + Math.sqrt(victimOldRating / attackerOldRating) + Math.sqrt((double) (victimKillCount + 10) / (killCount + 10));
        double attackerRelativeDecrease = attackerData.relative_decrease;
        double victimRelativeIncrease = playerData.relative_increase;
        double victimRelativeDecrease = playerData.relative_decrease + 1 / Math.sqrt(attackerOldRating / victimOldRating) + 1 / Math.sqrt((double) (killCount + 10) / (victimKillCount + 10));

        attackerData.relative_increase = attackerRelativeIncrease;
        attackerData.relative_decrease = attackerRelativeDecrease;
        playerData.relative_increase = victimRelativeIncrease;
        playerData.relative_decrease = victimRelativeDecrease;

        double attackerNewRating = (attackerRelativeIncrease + 20) / (attackerRelativeDecrease + 20);
        double victimNewRating = (victimRelativeIncrease + 20) / (victimRelativeDecrease + 20);

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

        return new double[]{attackerOldRating, attackerNewRating, victimOldRating, victimNewRating};
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

            String[] playerNames = new String[5];
            int[] scores = new int[5];

            Scoreboard scoreboard = minecraftServer.getScoreboard();
            Objective ratingObjective = scoreboard.getObjective("Rating");

            int i = 0;
            for (Score score : scoreboard.getPlayerScores(ratingObjective)) {
                if (i >= 5) break;
                playerNames[i] = score.getOwner();
                scores[i] = score.getScore();
                i++;
            }

            if (i < 5) {
                for (int j = i; j < 5; j++) {
                    playerNames[j] = "N/A";
                    scores[j] = 0;
                }
            }

            double x = 0.5;
            double y = 81.8;
            double z = -5.5;
            createArmorStand(level, x, y + 1.25, z, LegacyChatFormat.brandColor1 + "LEADERBOARD");
            createArmorStand(level, x, y + 1, z, LegacyChatFormat.brandColor1 + "MOST POINTS");
            createArmorStand(level, x, y + 0.5, z, LegacyChatFormat.brandColor2 + "#1 " + playerNames[0] + " " + scores[0]);
            createArmorStand(level, x, y + 0.25, z, LegacyChatFormat.brandColor2 + "#2 " + playerNames[1] + " " + scores[1]);
            createArmorStand(level, x, y, z, LegacyChatFormat.brandColor2 + "#3 " + playerNames[2] + " " + scores[2]);
            createArmorStand(level, x, y - 0.25, z, LegacyChatFormat.brandColor2 + "#4 " + playerNames[3] + " " + scores[3]);
            createArmorStand(level, x, y - 0.5, z, LegacyChatFormat.brandColor2 + "#5 " + playerNames[4] + " " + scores[4]);
        }
    }

    private static void createArmorStand(ServerLevel level, double x, double y, double z, String customName) {
        ArmorStand armorStand = new ArmorStand(level, x, y, z);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setCustomName(new TextComponent(customName));
        armorStand.setCustomNameVisible(true);
    }
}