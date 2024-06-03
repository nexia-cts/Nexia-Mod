package com.nexia.ffa.kits.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.ffa.KitGUI;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.kits.FfaKit;
import net.minecraft.network.chat.TextComponent;
import com.nexia.ffa.kits.utilities.player.PlayerData;
import com.nexia.ffa.kits.utilities.player.PlayerDataManager;
import com.nexia.ffa.kits.utilities.player.SavedPlayerData;
import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.nexia.ffa.kits.utilities.FfaAreas.*;

public class FfaKitsUtil {

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_kits") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.KITS;
    }

    public static double[] calculateKill(ServerPlayer attacker, ServerPlayer player) {
        attacker.heal(attacker.getMaxHealth());

        FfaKitsUtil.clearArrows(attacker);
        FfaKitsUtil.clearSpectralArrows(attacker);
        FfaKitsUtil.clearThrownTridents(attacker);

        if (player.getTags().contains("bot") || attacker.getTags().contains("bot")) return new double[0];

        SavedPlayerData data = PlayerDataManager.get(attacker).savedData;
        SavedPlayerData playerData = PlayerDataManager.get(player).savedData;

        // Counting the number of kills and encounters
        int killCount = KillTracker.getKillCount(attacker.getUUID(), player.getUUID());
        int victimKillCount = KillTracker.getKillCount(player.getUUID(), attacker.getUUID());

        // START RATING SYSTEM
        double attackerOldRating = data.rating;
        double victimOldRating = playerData.rating;

        double killWeight = ((victimOldRating / attackerOldRating) / 2) + ((double) ((victimKillCount + 5) / (killCount + 5)) / 2);
        double deathWeight = ((attackerOldRating / victimOldRating) / 2) + ((double) ((killCount + 5) / (victimKillCount + 5)) / 2);

        double attackerRelativeIncrease = data.relative_increase + Math.sqrt(killWeight);
        double attackerRelativeDecrease = data.relative_decrease;
        double victimRelativeIncrease = playerData.relative_increase;
        double victimRelativeDecrease = playerData.relative_decrease + 1/Math.sqrt(deathWeight);

        data.relative_increase = attackerRelativeIncrease;
        data.relative_decrease = attackerRelativeDecrease;
        playerData.relative_increase = victimRelativeIncrease;
        playerData.relative_decrease = victimRelativeDecrease;

        double attackerNewRating = (attackerRelativeIncrease + 5) / (attackerRelativeDecrease + 5);
        double victimNewRating = (victimRelativeIncrease + 5) / (victimRelativeDecrease + 5);

        data.rating = attackerNewRating;
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
        // END RATING SYSTEM

        data.killstreak++;
        if (data.killstreak > data.bestKillstreak) {
            data.bestKillstreak = data.killstreak;
        }
        data.kills++;

        // Increment kill count for attacker
        KillTracker.incrementKillCount(attacker.getUUID(), player.getUUID());

        if (data.killstreak % 5 == 0) {
            for (ServerPlayer serverPlayer : ffaWorld.players()) {
                PlayerUtil.getFactoryPlayer(serverPlayer).sendMessage(
                        Component.text("[")
                                .color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(attacker.getScoreboardName()).color(ChatFormat.normalColor))
                                .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text("!").color(ChatFormat.chatColor2))
                );
            }
        }
        return new double[]{attackerOldRating, attackerNewRating, victimOldRating, victimNewRating};
    }

    public static void fiveTick() {
        if (ffaWorld == null) return;
        if (ffaWorld.players().isEmpty()) return;
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {

            if (!com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(minecraftPlayer) && PlayerDataManager.get(minecraftPlayer).kit == null) {
                PlayerUtil.getFactoryPlayer(minecraftPlayer).sendTitle(Title.title(Component.text("No kit selected!").color(ChatFormat.failColor), Component.text("You need to select a kit!").color(ChatFormat.failColor)));
                PlayerUtil.sendSound(minecraftPlayer, new EntityPos(minecraftPlayer), SoundEvents.NOTE_BLOCK_DIDGERIDOO, SoundSource.BLOCKS, 10, 1);
                FfaKitsUtil.sendToSpawn(minecraftPlayer);
                return;
            }

            if (wasInSpawn.contains(minecraftPlayer.getUUID()) && !com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(minecraftPlayer)) {
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                wasInSpawn.remove(minecraftPlayer.getUUID());
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your kit was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void calculateDeath(ServerPlayer player) {
        if (player.getTags().contains("bot")) return;

        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if (data.killstreak > data.bestKillstreak) {
            data.bestKillstreak = data.killstreak;
        }

        if (data.killstreak >= 5) {
            for (ServerPlayer serverPlayer : FfaAreas.ffaWorld.players()) {
                PlayerUtil.getFactoryPlayer(serverPlayer).sendMessage(
                        Component.text("[")
                                .color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getScoreboardName()).color(ChatFormat.normalColor))
                                .append(Component.text(" has lost their killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text(".").color(ChatFormat.chatColor2))
                );
            }
        }
        data.killstreak = 0;
    }

    public static void clearThrownTridents(ServerPlayer player) {
        BlockPos c1 = ffaCorner1.offset(-10, -ffaCorner1.getY(), -10);
        BlockPos c2 = ffaCorner2.offset(10, 319 - ffaCorner2.getY(), 10);
        AABB aabb = new AABB(c1, c2);
        Predicate<Entity> predicate = o -> true;
        for (ThrownTrident trident : ffaWorld.getEntities(EntityType.TRIDENT, aabb, predicate)) {
            if (trident.getOwner() != null && trident.getOwner().getUUID().equals(player.getUUID())) {
                trident.remove();
            }
        }
    }

    public static void clearArrows(ServerPlayer player) {
        BlockPos c1 = ffaCorner1.offset(-10, -ffaCorner1.getY(), -10);
        BlockPos c2 = ffaCorner2.offset(10, 319 - ffaCorner2.getY(), 10);
        AABB aabb = new AABB(c1, c2);
        Predicate<Entity> predicate = o -> true;
        for (Arrow arrow : ffaWorld.getEntities(EntityType.ARROW, aabb, predicate)) {
            if (arrow.getOwner() != null && arrow.getOwner().getUUID().equals(player.getUUID())) {
                arrow.remove();
            }
        }
    }

    // KillTracker class for tracking kill counts
    public static class KillTracker {
        private static final Map<UUID, Map<UUID, Integer>> killCounts = new HashMap<>();
        private static final Map<UUID, Map<UUID, Integer>> encounterCounts = new HashMap<>();

        public static void incrementKillCount(UUID attacker, UUID victim) {
            killCounts.computeIfAbsent(attacker, k -> new HashMap<>()).merge(victim, 1, Integer::sum);
            encounterCounts.computeIfAbsent(attacker, k -> new HashMap<>()).merge(victim, 1, Integer::sum);
            encounterCounts.computeIfAbsent(victim, k -> new HashMap<>()).merge(attacker, 1, Integer::sum);
        }

        public static int getKillCount(UUID attacker, UUID victim) {
            return killCounts.getOrDefault(attacker, new HashMap<>()).getOrDefault(victim, 0);
        }

    }

    public static void clearSpectralArrows(ServerPlayer player) {
        BlockPos c1 = ffaCorner1.offset(-10, -ffaCorner1.getY(), -10);
        BlockPos c2 = ffaCorner2.offset(10, 319 - ffaCorner2.getY(), 10);
        AABB aabb = new AABB(c1, c2);
        Predicate<Entity> predicate = o -> true;
        for (SpectralArrow arrow : ffaWorld.getEntities(EntityType.SPECTRAL_ARROW, aabb, predicate)) {
            if (arrow.getOwner() != null && arrow.getOwner().getUUID().equals(player.getUUID())) {
                arrow.remove();
            }
        }
    }

    public static boolean beforeDamage(ServerPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }


    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player);

        if (!leaving) FfaKitsUtil.setDeathMessage(player, source);

        if (attacker != null) {
            FfaKitsUtil.clearThrownTridents(attacker);
            FfaKitsUtil.clearArrows(attacker);
            FfaKitsUtil.clearSpectralArrows(attacker);
            FfaKit ffaKit = PlayerDataManager.get(attacker).kit;
            if(ffaKit != null) ffaKit.giveKit(attacker, false);
        }

        if(!leaving) FfaKitsUtil.sendToSpawn(player);
    }

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaKitsUtil.isFfaPlayer(player) || FfaKitsUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    public static net.kyori.adventure.text.@NotNull TextComponent setDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(minecraftPlayer);

        calculateDeath(minecraftPlayer);

        Component msg = FfaUtil.returnDeathMessage(minecraftPlayer, source);

        if (attacker != null && msg.toString().contains("somehow killed themselves") && attacker != minecraftPlayer) {
            Component component = FfaUtil.returnClassicDeathMessage(minecraftPlayer, attacker);
            if (component != null) msg = component;

            double[] ratings = calculateKill(attacker, minecraftPlayer);
            if (ratings != null) {
                double attackerOldRating = ratings[0];
                double attackerNewRating = ratings[1];
                double victimOldRating = ratings[2];
                double victimNewRating = ratings[3];

                double attackerRatingChange = attackerNewRating - attackerOldRating;
                double victimRatingChange = victimNewRating - victimOldRating;

                msg = msg.append(Component.text(" (Rating change: ")
                                .color(ChatFormat.chatColor2))
                        .append(Component.text(String.format("%.2f", victimRatingChange))
                                .color(ChatFormat.failColor))
                        .append(Component.text(" / ")
                                .color(ChatFormat.chatColor2))
                        .append(Component.text("+")
                                .color(ChatFormat.greenColor))
                        .append(Component.text(String.format("%.2f", attackerRatingChange))
                                .color(ChatFormat.greenColor))
                        .append(Component.text(")")
                                .color(ChatFormat.chatColor2));
            }
        }

        for (Player player : ServerTime.factoryServer.getPlayers()) {
            if (player.hasTag("ffa_kits")) player.sendMessage(msg);
        }
        return null;
    }


    public static void sendToSpawn(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);

        player.inventory.clearContent();
        FfaKitsUtil.clearThrownTridents(player);
        FfaKitsUtil.clearArrows(player);
        FfaKitsUtil.clearSpectralArrows(player);
        FfaKitsUtil.wasInSpawn.add(player.getUUID());

        player.setGameMode(GameType.ADVENTURE);
        FfaAreas.spawn.teleportPlayer(FfaAreas.ffaWorld, player);
        if(data.kit != null) data.kit.giveKit(player, true);
        else {
            BlfScheduler.delay(20, new BlfRunnable() {
                @Override
                public void run() {
                    KitGUI.openKitGUI(player);
                }
            });
        }
    }
}