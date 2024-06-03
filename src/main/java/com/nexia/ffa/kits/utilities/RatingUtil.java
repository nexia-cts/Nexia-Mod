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
        double victimRelativeDecrease = playerData.relative_decrease + 1/Math.sqrt(attackerOldRating / victimOldRating) + 1/Math.sqrt((double) (killCount + 10) / (victimKillCount + 10));

        attackerData.relative_increase = attackerRelativeIncrease;
        attackerData.relative_decrease = attackerRelativeDecrease;
        playerData.relative_increase = victimRelativeIncrease;
        playerData.relative_decrease = victimRelativeDecrease;

        double attackerNewRating = (attackerRelativeIncrease + 20) / (attackerRelativeDecrease + 20);
        double victimNewRating = (victimRelativeIncrease + 20) / (victimRelativeDecrease + 20);

        attackerData.rating = attackerNewRating;
        playerData.rating = victimNewRating;

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

            String[] playerNames = {"playerName", "playerName", "playerName", "playerName", "playerName"};
            int[] scores = {0, 0, 0, 0, 0};


            Scoreboard scoreboard = minecraftServer.getScoreboard();
            Objective ratingObjective = scoreboard.getObjective("Rating");

            int i = 0;
            for (Score score : scoreboard.getPlayerScores(ratingObjective)) {
                playerNames[i] = score.getOwner();
                scores[i] = score.getScore();

                i++;
                if (i > 4) {
                    break;
                }
            }


            double x = 0.5;
            double y = 82;
            double z = -5.5;
            ArmorStand titleArmorStand = new ArmorStand(level, x, y + 1.3, z);
            titleArmorStand.setInvisible(true);
            titleArmorStand.setNoGravity(true);
            titleArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor1 + "LEADERBOARD"));
            titleArmorStand.setCustomNameVisible(true);

            ArmorStand subTitleArmorStand = new ArmorStand(level, x, y + 1, z);
            subTitleArmorStand.setInvisible(true);
            subTitleArmorStand.setNoGravity(true);
            subTitleArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor1 + "MOST POINTS"));
            subTitleArmorStand.setCustomNameVisible(true);

            ArmorStand firstArmorStand = new ArmorStand(level, x, y + 0.5, z);
            firstArmorStand.setInvisible(true);
            firstArmorStand.setNoGravity(true);
            firstArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor2 + "#1 " + playerNames[0] + " " + scores[0]));
            firstArmorStand.setCustomNameVisible(true);

            ArmorStand secondArmorStand = new ArmorStand(level, x, y + 0.25, z);
            secondArmorStand.setInvisible(true);
            secondArmorStand.setNoGravity(true);
            secondArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor2 + "#2 " + playerNames[1] + " " + scores[1]));
            secondArmorStand.setCustomNameVisible(true);

            ArmorStand thirdArmorStand = new ArmorStand(level, x, y, z);
            thirdArmorStand.setInvisible(true);
            thirdArmorStand.setNoGravity(true);
            thirdArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor2 + "#3 " + playerNames[2] + " " + scores[2]));
            thirdArmorStand.setCustomNameVisible(true);

            ArmorStand fourthArmorStand = new ArmorStand(level, x, y - 0.25, z);
            fourthArmorStand.setInvisible(true);
            fourthArmorStand.setNoGravity(true);
            fourthArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor2 + "#4 " + playerNames[3] + " " + scores[3]));
            fourthArmorStand.setCustomNameVisible(true);

            ArmorStand fifthArmorStand = new ArmorStand(level, x, y - 0.5, z);
            fifthArmorStand.setInvisible(true);
            fifthArmorStand.setNoGravity(true);
            fifthArmorStand.setCustomName(new TextComponent(LegacyChatFormat.brandColor2 + "#5 " + playerNames[4] + " " + scores[4]));
            fifthArmorStand.setCustomNameVisible(true);
        }
    }
}