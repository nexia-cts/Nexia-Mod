package com.nexia.ffa.uhc.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.google.gson.Gson;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.uhc.utilities.player.PlayerDataManager;
import com.nexia.ffa.uhc.utilities.player.SavedPlayerData;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
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

import static com.nexia.ffa.uhc.utilities.FfaAreas.*;
import static com.nexia.ffa.uhc.utilities.player.PlayerDataManager.dataDirectory;

public class FfaUhcUtil {

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static boolean isFfaPlayer(NexiaPlayer player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.hasTag("ffa_uhc") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.UHC;
    }

    public static boolean beforeDamage(NexiaPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }


    public static void calculateKill(NexiaPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.killstreak++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }
        data.kills++;

        BlfScheduler.delay(5, new BlfRunnable() {
            @Override
            public void run() {
                player.setHealth(player.unwrap().getMaxHealth());
            }
        });

        FfaUhcUtil.clearArrows(player);
        FfaUhcUtil.clearTrident(player);

        if(data.killstreak % 5 == 0) {
            for (ServerPlayer serverPlayer : FfaAreas.ffaWorld.players()) {
                new NexiaPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getRawName()).color(ChatFormat.normalColor))
                                .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text("!").color(ChatFormat.chatColor2))
                );
            }
        }
    }

    public static void fiveTick() {
        if (ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(minecraftPlayer);
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !FfaAreas.isInFfaSpawn(nexiaPlayer)){
                wasInSpawn.remove(minecraftPlayer.getUUID());
                saveInventory(nexiaPlayer);
                nexiaPlayer.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void saveInventory(NexiaPlayer player){
        // /config/nexia/ffa/uhc/inventory/savedInventories/uuid.json

        SavableInventory savableInventory = new SavableInventory(player.unwrap().inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = dataDirectory + "/inventory/savedInventories/" + player.getUUID() + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            LobbyUtil.returnToLobby(player, true);
            player.sendMessage(Component.text("Failed to set UHC FFA inventory. Please try again or contact a developer.", ChatFormat.failColor));
            return;
        }
    }

    public static void setInventory(NexiaPlayer player){

        // /config/nexia/ffa/uhc/inventory/savedInventories/uuid.json
        // /config/nexia/ffa/uhc/inventory/default.json

        if (!isFfaPlayer(player)) return;

        SavableInventory defaultInventory = null;
        SavableInventory layout = null;

        try {
            String file = dataDirectory + "/inventory";
            String defaultJson = Files.readString(Path.of(file + "/default.json"));
            Gson gson = new Gson();
            defaultInventory = gson.fromJson(defaultJson, SavableInventory.class);

            String layoutPath = String.format(file + "/savedInventories/%s.json", player.getUUID());
            if(new File(layoutPath).exists()) {
                String layoutJson = Files.readString(Path.of(layoutPath));
                layout = gson.fromJson(layoutJson, SavableInventory.class);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        if(defaultInventory == null) {
            LobbyUtil.returnToLobby(player, true);
            player.sendMessage(Component.text("Failed to set UHC FFA inventory. Please try again or contact a developer.", ChatFormat.failColor));
            return;
        }

        if(layout != null) {
            InventoryMerger.mergeSafe(player.unwrap(), layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.unwrap().inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        player.refreshInventory();
    }

    public static boolean beforeBuild(NexiaPlayer player, BlockPos blockPos) {
        if (player.unwrap().isCreative()) return true;
        if (wasInSpawn.contains(player.getUUID())) return false;
        return blockPos.getY() < FfaAreas.buildLimitY;
    }


    public static void calculateDeath(NexiaPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }

        if(data.killstreak >= 5) {
            for (ServerPlayer serverPlayer : FfaAreas.ffaWorld.players()) {
                new NexiaPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getRawName()).color(ChatFormat.normalColor))
                                .append(Component.text(" has lost their killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text(".").color(ChatFormat.chatColor2))
                );
            }
        }
        data.killstreak = 0;
    }

    public static void clearArrows(NexiaPlayer player) {
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

    public static void clearTrident(NexiaPlayer player) {
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

    public static void leaveOrDie(@NotNull NexiaPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.unwrap());

        if (attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);
            FfaUhcUtil.clearArrows(nexiaAttacker);
            FfaUhcUtil.clearTrident(nexiaAttacker);
            FfaUhcUtil.setInventory(nexiaAttacker);
        }

        if(!leaving){
            FfaUhcUtil.setDeathMessage(player, source);
            FfaUhcUtil.sendToSpawn(player);
        }
    }

    public static boolean canGoToSpawn(NexiaPlayer player) {
        if(!FfaUhcUtil.isFfaPlayer(player) || FfaUhcUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    public static void setDeathMessage(@NotNull NexiaPlayer minecraftPlayer, @Nullable DamageSource source) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(minecraftPlayer.unwrap());

        calculateDeath(minecraftPlayer);

        Component msg = FfaUtil.returnDeathMessage(minecraftPlayer, source);

        if(attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);
            if(msg.toString().contains("somehow killed themselves") && !nexiaAttacker.equals(minecraftPlayer)) {
                Component component = FfaUtil.returnClassicDeathMessage(minecraftPlayer, nexiaAttacker);
                if(component != null) msg = component;

                calculateKill(nexiaAttacker);
            }
        }

        for (Player player : ServerTime.factoryServer.getPlayers()) {
            if (player.hasTag("ffa_uhc")) player.sendMessage(msg);
        }
    }

    public static void sendToSpawn(NexiaPlayer player) {
        player.safeReset(true, Minecraft.GameMode.SURVIVAL);
        FfaUhcUtil.clearArrows(player);
        FfaUhcUtil.clearTrident(player);
        FfaUhcUtil.setInventory(player);
        FfaUhcUtil.wasInSpawn.add(player.getUUID());

        FfaAreas.spawn.teleportPlayer(FfaAreas.ffaWorld, player.unwrap());

        if(shouldResetMap) {
            BlfScheduler.delay(30, new BlfRunnable() {
                @Override
                public void run() {
                    FfaAreas.resetMap(true);
                }
            });
            shouldResetMap = false;
        }
    }
}
