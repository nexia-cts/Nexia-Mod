package com.nexia.ffa.classic.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.google.gson.Gson;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.player.SavedPlayerData;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Predicate;

import static com.nexia.ffa.classic.utilities.FfaAreas.*;
import static com.nexia.ffa.classic.utilities.player.PlayerDataManager.dataDirectory;

public class FfaClassicUtil {
    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static boolean beforeDamage(ServerPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_classic") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.CLASSIC;
    }

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaClassicUtil.isFfaPlayer(player) || FfaClassicUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    public static void fiveTick() {
        if(ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !FfaAreas.isInFfaSpawn(minecraftPlayer)){
                wasInSpawn.remove(minecraftPlayer.getUUID());
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                saveInventory(minecraftPlayer);
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }
    public static void saveInventory(ServerPlayer player){
        // /config/nexia/ffa/classic/inventory/savedInventories/uuid.json

        SavableInventory savableInventory = new SavableInventory(player.inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = dataDirectory + "/inventory/savedInventories/" + player.getStringUUID() + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
            player.sendMessage(LegacyChatFormat.format("{f}Failed to save Classic FFA inventory. Please try again or contact a developer."), Util.NIL_UUID);
            return;
        }
    }

    public static void calculateKill(ServerPlayer attacker, ServerPlayer player){
        BlfScheduler.delay(5, new BlfRunnable() {
            @Override
            public void run() {
                attacker.heal(attacker.getMaxHealth());
            }
        });

        if(player.getTags().contains("bot") || attacker.getTags().contains("bot")) return;

        SavedPlayerData data = PlayerDataManager.get(attacker).savedData;

        RatingUtil.calculateRating(attacker, player);
        RatingUtil.updateLeaderboard();

        data.killstreak++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }
        data.kills++;

        if(data.killstreak % 5 == 0){
            for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                if(FfaClassicUtil.isFfaPlayer(serverPlayer)) {
                    PlayerUtil.getFactoryPlayer(serverPlayer).sendMessage(
                            Component.text("[").color(ChatFormat.lineColor)
                                    .append(Component.text("☠").color(ChatFormat.failColor))
                                    .append(Component.text("] ").color(ChatFormat.lineColor))
                                    .append(Component.text(attacker.getScoreboardName()).color(ChatFormat.normalColor))
                                    .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                    .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                    .append(Component.text("!").color(ChatFormat.chatColor2))
                    );
                }
            }
        }
    }

    public static void calculateDeath(ServerPlayer player){

        if(player.getTags().contains("bot")) return;

        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }

        if(data.killstreak >= 5) {
            for (ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
                if (FfaClassicUtil.isFfaPlayer(serverPlayer)) {
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
        }
        data.killstreak = 0;
    }

    public static void setDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source){
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(minecraftPlayer);

        calculateDeath(minecraftPlayer);

        Component msg = FfaUtil.returnDeathMessage(minecraftPlayer, source);

        if(attacker != null && msg.toString().contains("somehow killed themselves") && attacker != minecraftPlayer) {

            Component component = FfaUtil.returnClassicDeathMessage(minecraftPlayer, attacker);
            if(component != null) msg = component;

            double attackerOldRating = com.nexia.ffa.kits.utilities.player.PlayerDataManager.get(attacker).savedData.rating;
            double victimOldRating = com.nexia.ffa.kits.utilities.player.PlayerDataManager.get(minecraftPlayer).savedData.rating;

            calculateKill(attacker, minecraftPlayer);

            double attackerNewRating = com.nexia.ffa.kits.utilities.player.PlayerDataManager.get(attacker).savedData.rating;
            double victimNewRating = com.nexia.ffa.kits.utilities.player.PlayerDataManager.get(minecraftPlayer).savedData.rating;

            msg = msg.append(Component.text(" (")
                            .color(ChatFormat.chatColor2))
                    .append(Component.text(String.format("%.2f", RatingUtil.calculateRatingDifference(victimNewRating, victimOldRating)))
                            .color(ChatFormat.failColor))
                    .append(Component.text(" / ")
                            .color(ChatFormat.chatColor2))
                    .append(Component.text("+")
                            .color(ChatFormat.greenColor))
                    .append(Component.text(String.format("%.2f", RatingUtil.calculateRatingDifference(attackerNewRating, attackerOldRating)))
                            .color(ChatFormat.greenColor))
                    .append(Component.text(")")
                            .color(ChatFormat.chatColor2));
        }

        for (Player player : ServerTime.factoryServer.getPlayers()) {
            if (player.hasTag("ffa_classic")) player.sendMessage(msg);
        }
    }

    public static void setInventory(ServerPlayer player){

        // /config/nexia/ffa/classic/inventory/savedInventories/uuid.json
        // /config/nexia/ffa/classic/inventory/default.json

        SavableInventory defaultInventory = null;
        SavableInventory layout = null;

        try {
            String file = dataDirectory + "/inventory";
            String defaultJson = Files.readString(Path.of(file + "/default.json"));
            Gson gson = new Gson();
            defaultInventory = gson.fromJson(defaultJson, SavableInventory.class);

            String layoutPath = String.format(file + "/savedInventories/%s.json", player.getStringUUID());
            if(new File(layoutPath).exists()) {
                String layoutJson = Files.readString(Path.of(layoutPath));
                layout = gson.fromJson(layoutJson, SavableInventory.class);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        if(defaultInventory == null) {
            ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
            player.sendMessage(LegacyChatFormat.format("{f}Failed to set Classic FFA inventory. Please try again or contact a developer."), Util.NIL_UUID);
            return;
        }

        if(layout != null) {
            InventoryMerger.mergeSafe(player, layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        ItemStackUtil.sendInventoryRefreshPacket(player);
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

    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player);

        if (attacker != null) {
            FfaClassicUtil.clearThrownTridents(attacker);
            FfaClassicUtil.setInventory(attacker);
        }

        FfaClassicUtil.clearThrownTridents(player);

        if(leaving) return;

        FfaClassicUtil.setDeathMessage(player, source);
        FfaClassicUtil.setInventory(player);
        FfaClassicUtil.wasInSpawn.add(player.getUUID());
    }
}
