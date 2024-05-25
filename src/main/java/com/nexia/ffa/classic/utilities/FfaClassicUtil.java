package com.nexia.ffa.classic.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.google.gson.Gson;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.AABB;
import net.notcoded.codelib.players.AccuratePlayer;
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

    public static boolean isFfaPlayer(NexiaPlayer player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.player().get().getTags().contains("ffa_classic") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.CLASSIC;
    }

    public static boolean canGoToSpawn(NexiaPlayer player) {
        if(!FfaClassicUtil.isFfaPlayer(player) || FfaClassicUtil.wasInSpawn.contains(player.player().uuid)) return true;
        return !(Math.round(player.player().get().getHealth()) < 20);
    }

    public static void fiveTick() {
        if(ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
        for (ServerPlayer player : ffaWorld.players()) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(player));
            if(wasInSpawn.contains(player.getUUID()) && !FfaAreas.isInFfaSpawn(nexiaPlayer)){
                wasInSpawn.remove(player.getUUID());
                saveInventory(nexiaPlayer);
                nexiaPlayer.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }
    public static void saveInventory(NexiaPlayer player){
        // /config/nexia/ffa/classic/inventory/savedInventories/uuid.json

        SavableInventory savableInventory = new SavableInventory(player.player().get().inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = dataDirectory + "/inventory/savedInventories/" + player.player().uuid + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            LobbyUtil.returnToLobby(player, true);
            player.sendMessage(Component.text("Failed to save Classic FFA inventory. Please try again or contact a developer.").color(ChatFormat.failColor));
            return;
        }
    }

    public static void calculateKill(NexiaPlayer attacker, NexiaPlayer player){
        BlfScheduler.delay(5, new BlfRunnable() {
            @Override
            public void run() {
                attacker.player().get().setHealth(attacker.player().get().getHealth());
            }
        });

        if(player.getFactoryPlayer().hasTag("bot") || attacker.getFactoryPlayer().hasTag("bot")) return;

        SavedPlayerData data = PlayerDataManager.get(attacker).savedData;
        data.killstreak++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }
        data.kills++;

        if(data.killstreak % 5 == 0){
            for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(serverPlayer));
                if(FfaClassicUtil.isFfaPlayer(nexiaPlayer)) {
                    nexiaPlayer.sendMessage(
                            Component.text("[").color(ChatFormat.lineColor)
                                    .append(Component.text("☠").color(ChatFormat.failColor))
                                    .append(Component.text("] ").color(ChatFormat.lineColor))
                                    .append(Component.text(attacker.player().name).color(ChatFormat.normalColor))
                                    .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                    .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                    .append(Component.text("!").color(ChatFormat.chatColor2))
                    );
                }
            }
        }
    }

    public static void calculateDeath(NexiaPlayer player){

        if(player.getFactoryPlayer().hasTag("bot")) return;

        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }

        if(data.killstreak >= 5) {
            for (ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
                NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(serverPlayer));
                if (FfaClassicUtil.isFfaPlayer(nexiaPlayer)) {
                    nexiaPlayer.sendMessage(
                            Component.text("[").color(ChatFormat.lineColor)
                                    .append(Component.text("☠").color(ChatFormat.failColor))
                                    .append(Component.text("] ").color(ChatFormat.lineColor))
                                    .append(Component.text(player.player().name).color(ChatFormat.normalColor))
                                    .append(Component.text(" has lost their killstreak of ").color(ChatFormat.chatColor2))
                                    .append(Component.text(data.killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                    .append(Component.text(".").color(ChatFormat.chatColor2))
                    );
                }
            }
        }
        data.killstreak = 0;
    }

    public static boolean beforeDamage(NexiaPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }


    public static void setDeathMessage(@NotNull NexiaPlayer player, @Nullable DamageSource source){
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.player().get());

        calculateDeath(player);

        Component msg = FfaUtil.returnDeathMessage(player, source);

        if(attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(new AccuratePlayer(attacker));
            if(msg.toString().contains("somehow killed themselves") && !nexiaAttacker.equals(player)) {
                Component component = FfaUtil.returnClassicDeathMessage(player, nexiaAttacker);
                if(component != null) msg = component;

                calculateKill(nexiaAttacker, player);
            }
        }

        for (Player fPlayer : ServerTime.factoryServer.getPlayers()) {
            if (fPlayer.hasTag("ffa_classic")) player.sendMessage(msg);
        }
    }

    public static void setInventory(NexiaPlayer player){

        // /config/nexia/ffa/classic/inventory/savedInventories/uuid.json
        // /config/nexia/ffa/classic/inventory/default.json

        SavableInventory defaultInventory = null;
        SavableInventory layout = null;

        try {
            String file = dataDirectory + "/inventory";
            String defaultJson = Files.readString(Path.of(file + "/default.json"));
            Gson gson = new Gson();
            defaultInventory = gson.fromJson(defaultJson, SavableInventory.class);

            String layoutPath = String.format(file + "/savedInventories/%s.json", player.player().uuid);
            if(new File(layoutPath).exists()) {
                String layoutJson = Files.readString(Path.of(layoutPath));
                layout = gson.fromJson(layoutJson, SavableInventory.class);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        if(defaultInventory == null) {
            LobbyUtil.returnToLobby(player, true);
            player.sendMessage(Component.text("Failed to set Classic FFA inventory. Please try again or contact a developer.").color(ChatFormat.failColor));
            return;
        }

        if(layout != null) {
            InventoryMerger.mergeSafe(player.player().get(), layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.player().get().inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        player.refreshInventory();
    }

    public static void clearThrownTridents(NexiaPlayer player) {
        BlockPos c1 = ffaCorner1.offset(-10, -ffaCorner1.getY(), -10);
        BlockPos c2 = ffaCorner2.offset(10, 319 - ffaCorner2.getY(), 10);
        AABB aabb = new AABB(c1, c2);
        Predicate<Entity> predicate = o -> true;
        for (ThrownTrident trident : ffaWorld.getEntities(EntityType.TRIDENT, aabb, predicate)) {
            if (trident.getOwner() != null && trident.getOwner().getUUID().equals(player.player().uuid)) {
                trident.remove();
            }
        }
    }

    public static void leaveOrDie(@NotNull NexiaPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.player().get());

        if (attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(new AccuratePlayer(attacker));
            clearThrownTridents(nexiaAttacker);
            setInventory(nexiaAttacker);
        }

        clearThrownTridents(player);

        if(leaving) return;

        FfaClassicUtil.setDeathMessage(player, source);
        FfaClassicUtil.setInventory(player);
        FfaClassicUtil.wasInSpawn.add(player.player().uuid);
    }
}
