package com.nexia.ffa.base;

import com.google.gson.Gson;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.RatingUtil;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.api.world.util.BoundingBox;
import com.nexia.nexus.api.world.util.Vector3D;
import com.nexia.nexus.builder.implementation.world.entity.projectile.WrappedProjectile;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseFfaUtil {
    public ArrayList<UUID> wasInSpawn = new ArrayList<>();
    public KillTracker killTracker = new KillTracker();
    public static final List<BaseFfaUtil> ffaUtils = new ArrayList<>();

    public BaseFfaUtil() {
        ffaUtils.add(this);
    }

    public abstract String getName();

    public String getNameLowercase() {
        return getName().toLowerCase();
    }

    public abstract FfaGameMode getGameMode();

    public abstract PlayerDataManager getDataManager();

    public abstract ServerLevel getFfaWorld();

    public abstract World getNexusFfaWorld();

    public abstract EntityPos getSpawn();

    public KillTracker getKillTracker() {
        return killTracker;
    }

    public boolean isFfaPlayer(NexiaPlayer player) {
        CorePlayerData data = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
        return player.hasTag("ffa_" + getNameLowercase()) && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == getGameMode();
    }

    public boolean canGoToSpawn(NexiaPlayer player) {
        if(!isFfaPlayer(player) || wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    private boolean checkWorld() {
        if(getFfaWorld() == null) return true;
        List<ServerPlayer> players = getFfaWorld().players();
        if(players.isEmpty()) return true;

        return checkBot(players);
    }
    
    public boolean checkBot(List<ServerPlayer> players) {
        return false;
    }

    public void fiveTick() {
        if(checkWorld()) return;
        for (Player player : getNexusFfaWorld().getPlayers()) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
            if(!isFfaPlayer(nexiaPlayer)) continue;
            
            completeFiveTick(nexiaPlayer);
        }
    }

    public abstract void completeFiveTick(NexiaPlayer player);

    public void saveInventory(NexiaPlayer player){
        SavableInventory savableInventory = new SavableInventory(player.unwrap().inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = getDataManager().getDataDirectory() + "/inventory/savedInventories/" + player.getUUID() + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            LobbyUtil.returnToLobby(player, true);
            player.sendMessage(Component.text("Failed to save " + getName() + " FFA inventory. Please try again or contact a developer.").color(ChatFormat.failColor));
        }
    }

    public void calculateKill(NexiaPlayer attacker, NexiaPlayer player){
        ServerTime.scheduler.schedule(() -> attacker.setHealth(attacker.getMaxHealth()), 5);

        doPreKill(attacker, player);

        if(player.hasTag("bot") || attacker.hasTag("bot")) return;

        SavedPlayerData data = getDataManager().get(attacker).savedData;

        RatingUtil.calculateRating(attacker, player, this);
        RatingUtil.updateLeaderboard(this);

        data.incrementInteger("killstreak");
        int killstreak = data.get(Integer.class, "killstreak");
        if (killstreak > data.get(Integer.class, "bestKillstreak"))
            data.set(Integer.class, "bestKillstreak", killstreak);
        data.incrementInteger("kills");

        killHeal(attacker);
        giveKillLoot(attacker, player);

        // Increment kill count for attacker
        getKillTracker().incrementKillCount(attacker.getUUID(), player.getUUID());

        if (killstreak % 5 == 0) {
            for (ServerPlayer serverPlayer : getFfaWorld().players()) {
                new NexiaPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(attacker.getRawName()).color(ChatFormat.normalColor))
                                .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text("!").color(ChatFormat.chatColor2))
                );
            }
        }
    }

    public void giveKillLoot(NexiaPlayer attacker, NexiaPlayer player) {
    }

    public void killHeal(NexiaPlayer attacker) {
    }

    public void doPreKill(NexiaPlayer attacker, NexiaPlayer player) {
    }

    public void calculateDeath(NexiaPlayer player){
        if (player.hasTag("bot")) return;

        SavedPlayerData data = getDataManager().get(player).savedData;
        data.incrementInteger("deaths");
        int killstreak = data.get(Integer.class, "killstreak");
        if (killstreak > data.get(Integer.class, "bestKillstreak"))
            data.set(Integer.class, "bestKillstreak", killstreak);

        if (killstreak >= 5) {
            for (ServerPlayer serverPlayer : getFfaWorld().players()) {
                new NexiaPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠").color(ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getRawName()).color(ChatFormat.normalColor))
                                .append(Component.text(" has lost their killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(killstreak).color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text(".").color(ChatFormat.chatColor2))
                );
            }
        }
        data.set(Integer.class, "killstreak", 0);
    }

    public boolean beforeDamage(NexiaPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }

    public abstract boolean isInFfaSpawn(NexiaPlayer player);

    public abstract BlockPos[] getFfaCorners();


    public void setDeathMessage(@NotNull NexiaPlayer player, @Nullable DamageSource source){
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.unwrap());

        calculateDeath(player);
        
        Component msg = FfaUtil.returnDeathMessage(player, source);

        if(attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);
            if(msg.toString().contains("somehow killed themselves") && !nexiaAttacker.equals(player)) {
                Component component = FfaUtil.returnClassicDeathMessage(player, nexiaAttacker);
                if(component != null) msg = component;

                double attackerOldRating = getDataManager().get(nexiaAttacker).savedData.get(Double.class, "rating");
                double victimOldRating = getDataManager().get(player).savedData.get(Double.class, "rating");

                calculateKill(nexiaAttacker, player);

                double attackerNewRating = getDataManager().get(nexiaAttacker).savedData.get(Double.class, "rating");
                double victimNewRating = getDataManager().get(player).savedData.get(Double.class, "rating");

                if(component != null) {
                    msg = msg.append(Component.text(" (")
                                    .color(ChatFormat.chatColor2))
                            .append(Component.text(String.format("%.2f", RatingUtil.calculateRatingDifference(victimNewRating, victimOldRating) * 100))
                                    .color(ChatFormat.failColor))
                            .append(Component.text(" / ")
                                    .color(ChatFormat.chatColor2))
                            .append(Component.text("+")
                                    .color(ChatFormat.greenColor))
                            .append(Component.text(String.format("%.2f", RatingUtil.calculateRatingDifference(attackerNewRating, attackerOldRating) * 100))
                                    .color(ChatFormat.greenColor))
                            .append(Component.text(")")
                                    .color(ChatFormat.chatColor2));
                }
            }
        }

        for (Player fPlayer : getNexusFfaWorld().getPlayers()) {
            fPlayer.sendMessage(msg);
        }
    }

    public void setInventory(NexiaPlayer player){
        if (!isFfaPlayer(player)) return;

        SavableInventory defaultInventory = null;
        SavableInventory layout = null;

        try {
            String file = getDataManager().getDataDirectory() + "/inventory";
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
            player.sendMessage(Component.text("Failed to set " + getName() + " FFA inventory. Please try again or contact a developer.").color(ChatFormat.failColor));
            return;
        }

        if(layout != null) {
            InventoryMerger.mergeSafe(player.unwrap(), layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.unwrap().inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        alterInventory(player);

        player.refreshInventory();
    }

    public void alterInventory(NexiaPlayer player) {

    }

    public void clearProjectiles(NexiaPlayer player) {
        BlockPos c1 = getFfaCorners()[0].offset(-10, -getFfaCorners()[0].getY(), -10);
        BlockPos c2 = getFfaCorners()[1].offset(10, 319 - getFfaCorners()[1].getY(), 10);
        BoundingBox box = new BoundingBox(c1.getX(), c1.getY(), c1.getZ(), c2.getX(), c2.getY(), c2.getZ());

        for (WrappedProjectile projectile : getNexusFfaWorld().getEntities(box, entity -> entity instanceof WrappedProjectile projectile
                        && projectile.getOwner() != null
                        && projectile.getOwner().getUUID().equals(player.getUUID()))
                .stream().map(WrappedProjectile.class::cast).toList()) {
            projectile.kill();
        }
    }

    public void clearThrownTridents(NexiaPlayer player) {
        BlockPos c1 = getFfaCorners()[0].offset(-10, -getFfaCorners()[0].getY(), -10);
        BlockPos c2 = getFfaCorners()[1].offset(10, 319 - getFfaCorners()[1].getY(), 10);
        AABB aabb = new AABB(c1, c2);
        for (ThrownTrident trident : getFfaWorld().getEntities(EntityType.TRIDENT, aabb, trident -> trident.getOwner() != null && trident.getOwner().getUUID().equals(player.getUUID()))) {
            trident.remove();
        }
    }

    public void clearArrows(NexiaPlayer player) {
        BlockPos c1 = getFfaCorners()[0].offset(-10, - getFfaCorners()[0].getY(), -10);
        BlockPos c2 = getFfaCorners()[1].offset(10, 319 - getFfaCorners()[1].getY(), 10);
        AABB aabb = new AABB(c1, c2);
        for (Arrow arrow : getFfaWorld().getEntities(EntityType.ARROW, aabb, arrow -> arrow.getOwner() != null && arrow.getOwner().getUUID().equals(player.getUUID()))) {
            arrow.remove();
        }
    }

    public void clearSpectralArrows(NexiaPlayer player) {
        BlockPos c1 = getFfaCorners()[0].offset(-10, - getFfaCorners()[0].getY(), -10);
        BlockPos c2 = getFfaCorners()[1].offset(10, 319 - getFfaCorners()[1].getY(), 10);
        AABB aabb = new AABB(c1, c2);
        for (SpectralArrow arrow : getFfaWorld().getEntities(EntityType.SPECTRAL_ARROW, aabb, arrow -> arrow.getOwner() != null && arrow.getOwner().getUUID().equals(player.getUUID()))) {
            arrow.remove();
        }
    }

    public void clearEnderpearls(NexiaPlayer player) {
        BlockPos c1 = getFfaCorners()[0].offset(-10, - getFfaCorners()[0].getY(), -10);
        BlockPos c2 = getFfaCorners()[1].offset(10, 319 - getFfaCorners()[1].getY(), 10);
        AABB aabb = new AABB(c1, c2);
        for (ThrownEnderpearl enderpearl : getFfaWorld().getEntities(EntityType.ENDER_PEARL, aabb, enderpearl -> enderpearl.getOwner() != null && enderpearl.getOwner().getUUID().equals(player.getUUID()))) {
            enderpearl.remove();
        }
    }

    public void leaveOrDie(@NotNull NexiaPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.unwrap());

        if (attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);

            clearThrownTridents(nexiaAttacker);
            clearArrows(nexiaAttacker);
            clearSpectralArrows(nexiaAttacker);
            setInventory(nexiaAttacker);
        }

        clearThrownTridents(player);

        if(leaving) return;

        setDeathMessage(player, source);
        sendToSpawn(player);
    }

    public void sendToSpawn(NexiaPlayer player) {
        player.getInventory().clear();
        clearProjectiles(player);
        wasInSpawn.add(player.getUUID());

        player.safeReset(true, getMinecraftGameMode());
        getSpawn().teleportPlayer(getNexusFfaWorld(), player);
        player.setVelocity(new Vector3D(0, 0, 0));
        finishSendToSpawn(player);
    }

    public Minecraft.GameMode getMinecraftGameMode() {
        return Minecraft.GameMode.ADVENTURE;
    }

    public abstract void finishSendToSpawn(NexiaPlayer player);

    public boolean beforeBuild(NexiaPlayer player, BlockPos blockPos) {
        return true;
    }
}
