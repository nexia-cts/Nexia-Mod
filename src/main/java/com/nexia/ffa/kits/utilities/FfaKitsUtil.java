package com.nexia.ffa.kits.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.gui.ffa.KitGUI;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.kits.FfaKit;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.nexia.ffa.kits.utilities.FfaAreas.*;

public class FfaKitsUtil {

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_kits") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.KITS;
    }

    public static void ffaSecond() {
        if (ffaWorld == null || ffaWorld.players().isEmpty()) return;
        for (ServerPlayer player : ffaWorld.players()) {
            if (!isFfaPlayer(player)) continue;

            if (FfaAreas.isInFfaSpawn(player)) {
                player.addTag(LobbyUtil.NO_DAMAGE_TAG);
            } else {
                player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            }
        }
    }

    public static void calculateKill(ServerPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.killstreak++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }
        data.kills++;
        player.heal(player.getMaxHealth());

        FfaKitsUtil.clearArrows(player);
        FfaKitsUtil.clearSpectralArrows(player);
        FfaKitsUtil.clearThrownTridents(player);

        if(data.killstreak % 5 == 0) {
            for (ServerPlayer serverPlayer : FfaAreas.ffaWorld.players()) {
                PlayerUtil.getFactoryPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getScoreboardName()).color(ChatFormat.normalColor))
                                .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text("!").color(ChatFormat.chatColor2))
                );
            }
        }
    }

    public static void fiveTick() {
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {

            if(!com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(minecraftPlayer) && PlayerDataManager.get(minecraftPlayer).kit == null) {
                PlayerUtil.getFactoryPlayer(minecraftPlayer).sendTitle(Title.title(Component.text("No kit selected!").color(ChatFormat.failColor), Component.text("You need to select a kit!").color(ChatFormat.failColor)));
                PlayerUtil.sendSound(minecraftPlayer, new EntityPos(minecraftPlayer), SoundEvents.NOTE_BLOCK_DIDGERIDOO, SoundSource.BLOCKS, 10, 1);
                FfaKitsUtil.sendToSpawn(minecraftPlayer);
                return;
            }

            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !com.nexia.ffa.kits.utilities.FfaAreas.isInFfaSpawn(minecraftPlayer)){
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                wasInSpawn.remove(minecraftPlayer.getUUID());
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your kit was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void calculateDeath(ServerPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }

        if(data.killstreak >= 5) {
            for (ServerPlayer serverPlayer : FfaAreas.ffaWorld.players()) {
                PlayerUtil.getFactoryPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
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


    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = null;

        try {
            attacker = PlayerUtil.getPlayerAttacker(player, source.getEntity());
        } catch (Exception ignored) { }
        // there is probably a better way to do this but im too lazy to do that

        if(!leaving) FfaKitsUtil.setDeathMessage(player, source);

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
        return !(player.getHealth() < 20);
    }

    public static void setDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source) {
        ServerPlayer attacker = null;
        Entity fAttacker = null;

        if (source != null && source.getEntity() != null) {
            fAttacker = source.getEntity();
            if(PlayerUtil.getPlayerAttacker(minecraftPlayer, source.getEntity()) != null) attacker = PlayerUtil.getPlayerAttacker(minecraftPlayer, source.getEntity());
        }

        calculateDeath(minecraftPlayer);

        Component invalid = Component.text("Wow,").color(ChatFormat.chatColor2)
                .append(Component.text(" ☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor))
                .append(Component.text(" somehow killed themselves.").color(ChatFormat.chatColor2));

        Component msg = invalid;

        if (source == DamageSource.OUT_OF_WORLD) {
            //msg = LegacyChatFormat.format("§c⚐ {} §7took a ride to the void.", minecraftPlayer.getScoreboardName());
            msg = Component.text("⚐ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" took a ride to the void.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.ON_FIRE || source == DamageSource.LAVA) {
            //msg = LegacyChatFormat.format("§c\uD83D\uDD25 {} §7was deepfried in lava.", minecraftPlayer.getScoreboardName());
            msg = Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" was deepfried in lava.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.HOT_FLOOR) {
            //msg = LegacyChatFormat.format("§c\uD83D\uDD25 {} §7stepped on hot legos.", minecraftPlayer.getScoreboardName());
            msg = Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" stepped on hot legos.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
            msg = Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" comBusted.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.FALL) {
            msg = Component.text("⚓ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" turned into a human doormat.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.CACTUS) {
            //msg = LegacyChatFormat.format("§7ʕっ·ᴥ·ʔっ §c☠ {} §7hugged a cactus.", minecraftPlayer.getScoreboardName());
            msg = Component.text("ʕっ·ᴥ·ʔっ ").color(ChatFormat.chatColor2)
                    .append(Component.text("☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor))
                    .append(Component.text(" hugged a cactus.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.DROWN || source == DamageSource.DRY_OUT) {
            msg = Component.text("\uD83C\uDF0A " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" had a bit too much to drink.").color(ChatFormat.chatColor2));
        }

        if (source == DamageSource.MAGIC) {
            msg = Component.text("\uD83E\uDDEA " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" had a bit too much pot.").color(ChatFormat.chatColor2));
        }

        if (fAttacker instanceof Projectile && attacker != null) {
            msg = Component.text("\uD83C\uDFF9 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" experienced freedom by ").color(ChatFormat.chatColor2)
                            .append(Component.text(attacker.getScoreboardName()).color(ChatFormat.greenColor))
                    );
        }


        if(attacker != null && msg == invalid && attacker != minecraftPlayer) {

            String symbol = "◆";
            Item handItem = attacker.getMainHandItem().getItem();
            String itemName = new ItemStack(handItem).getDisplayName().toString().toLowerCase();

            if (itemName.contains("sword")) {
                symbol = "\uD83D\uDDE1";
            } else if (handItem == Items.TRIDENT) {
                symbol = "\uD83D\uDD31";
            } else if (itemName.contains("axe")) {
                symbol = "\uD83E\uDE93";
            }

            msg = Component.text("☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" was killed by ").color(ChatFormat.chatColor2))
                    .append(Component.text(symbol + " " + attacker.getScoreboardName()).color(ChatFormat.greenColor))
                    .append(Component.text(" with ").color(ChatFormat.chatColor2))
                    .append(Component.text(FfaUtil.calculateHealth(attacker.getHealth()) + "❤").color(ChatFormat.failColor))
                    .append(Component.text(" left.").color(ChatFormat.chatColor2));

            calculateKill(attacker);
        }

        for (Player player : ServerTime.factoryServer.getPlayers()) {
            if (player.hasTag("ffa_kits")) player.sendMessage(msg);
        }
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
        else KitGUI.openKitGUI(player);
    }
}
