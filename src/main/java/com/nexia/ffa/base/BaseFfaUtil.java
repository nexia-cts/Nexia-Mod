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
import com.nexia.ffa.FfaAreas;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.api.world.util.BoundingBox;
import com.nexia.nexus.api.world.util.Location;
import com.nexia.nexus.builder.implementation.world.entity.projectile.WrappedProjectile;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
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
    public final FfaAreas ffaAreas;
    public static final List<BaseFfaUtil> ffaUtils = new ArrayList<>();

    public BaseFfaUtil(FfaAreas ffaAreas) {
        this.ffaAreas = ffaAreas;
        ffaUtils.add(this);
    }

    public abstract String getName();

    public String getNameLowercase() {
        return getName().toLowerCase();
    }

    public abstract FfaGameMode getGameMode();

    public abstract PlayerDataManager getDataManager();

    public boolean isFfaWorld(Level level) {
        return ffaAreas.isFfaWorld(level);
    }

    public ServerLevel getFfaWorld() {
        return ffaAreas.getFfaWorld();
    }

    public World getNexusFfaWorld() {
        return ffaAreas.getNexusFfaWorld();
    }

    public EntityPos getSpawn() {
        return ffaAreas.getSpawn();
    }

    public Location getRespawnLocation() {
        return ffaAreas.getFfaLocation();
    }

    public boolean isFfaPlayer(NexiaPlayer player) {
        CorePlayerData data = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);
        return data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == getGameMode();
    }

    public boolean canGoToSpawn(NexiaPlayer player) {
        if(!isFfaPlayer(player) || wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    private boolean checkWorld() {
        if(getFfaWorld() == null) return true;
        List<ServerPlayer> players = getFfaWorld().players();
        if(players.isEmpty()) return true;

        return checkBot();
    }
    
    public boolean checkBot() {
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
            player.sendMessage(Component.text("Failed to save " + getName() + " FFA inventory. Please try again or contact a developer.", ChatFormat.failColor));
        }
    }

    public void calculateKill(NexiaPlayer attacker, NexiaPlayer player, boolean sendMessage) {
        killHeal(attacker);

        doPreKill(attacker, player);

        if(player.hasTag("bot") || attacker.hasTag("bot")) return;

        SavedPlayerData data = getDataManager().get(attacker).savedData;

        data.incrementInteger("killstreak");
        int killstreak = data.get(Integer.class, "killstreak");
        if (killstreak > data.get(Integer.class, "bestKillstreak"))
            data.set(Integer.class, "bestKillstreak", killstreak);
        data.incrementInteger("kills");

        giveKillLoot(attacker, player);

        if (killstreak % 5 == 0 && sendMessage) {
            for (ServerPlayer serverPlayer : getFfaWorld().players()) {
                new NexiaPlayer(serverPlayer).sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠", ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(attacker.getRawName(), ChatFormat.normalColor))
                                .append(Component.text(" now has a killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(killstreak, ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                .append(Component.text("!").color(ChatFormat.chatColor2))
                );
            }
        }
    }

    public void giveKillLoot(NexiaPlayer attacker, NexiaPlayer player) {
    }

    public void killHeal(NexiaPlayer attacker) {
        ServerTime.scheduler.schedule(() -> attacker.setHealth(attacker.getMaxHealth()), 5);
    }

    public void doPreKill(NexiaPlayer attacker, NexiaPlayer player) {
    }

    public void calculateDeath(NexiaPlayer player, boolean sendMessage){
        if (PlayerUtil.getPlayerAttacker(player.unwrap()).getTags().contains("bot")) return;

        SavedPlayerData data = getDataManager().get(player).savedData;
        data.incrementInteger("deaths");
        int killstreak = data.get(Integer.class, "killstreak");
        if (killstreak > data.get(Integer.class, "bestKillstreak"))
            data.set(Integer.class, "bestKillstreak", killstreak);

        if (killstreak >= 5 && sendMessage) {
            for (Player nexusPlayer : getNexusFfaWorld().getPlayers()) {
                nexusPlayer.sendMessage(
                        Component.text("[").color(ChatFormat.lineColor)
                                .append(Component.text("☠", ChatFormat.failColor))
                                .append(Component.text("] ").color(ChatFormat.lineColor))
                                .append(Component.text(player.getRawName(), ChatFormat.normalColor))
                                .append(Component.text(" has lost their killstreak of ").color(ChatFormat.chatColor2))
                                .append(Component.text(killstreak, ChatFormat.failColor).decoration(ChatFormat.bold, true))
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

    public boolean isInFfaSpawn(NexiaPlayer player) {
        return ffaAreas.isInFfaSpawn(player);
    }

    public AABB getFfaCorners() {
        return ffaAreas.getFfaCorners();
    }


    public void setDeathMessage(@NotNull NexiaPlayer player, @Nullable ServerPlayer attacker, @Nullable DamageSource source) {
        calculateDeath(player, true);

        Component msg = FfaUtil.returnDeathMessage(player, source);

        if (attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);
            if(msg.toString().contains("somehow killed themselves") && !nexiaAttacker.equals(player)) {
                Component component = FfaUtil.returnClassicDeathMessage(player, nexiaAttacker);
                if(component != null) msg = component;

                calculateKill(nexiaAttacker, player, true);
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
            player.sendMessage(Component.text("Failed to set " + getName() + " FFA inventory. Please try again or contact a developer.", ChatFormat.failColor));
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
        AABB aabb = getFfaCorners().contract(-10, -getFfaCorners().minY, -10).expandTowards(10, 319 - getFfaCorners().maxY, 10);
        BoundingBox box = new BoundingBox(aabb.minX, aabb.maxX, aabb.minY, aabb.maxY, aabb.minZ, aabb.maxZ);

        for (WrappedProjectile projectile : getNexusFfaWorld().getEntities(box, entity -> entity instanceof WrappedProjectile projectile
                        && projectile.getOwner() != null
                        && projectile.getOwner().getUUID().equals(player.getUUID()))
                .stream().map(WrappedProjectile.class::cast).toList()) {
            projectile.kill();
        }
    }

    public void leaveOrDie(@NotNull NexiaPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker;
        if(source != null && source.getEntity() != null) attacker = PlayerUtil.getPlayerAttacker(player.unwrap(), source.getEntity());
        else attacker = PlayerUtil.getPlayerAttacker(player.unwrap());
        wasInSpawn.remove(player.getUUID());

        if (attacker != null) {
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);

            clearProjectiles(nexiaAttacker);
            fulfilKill(player, source, nexiaAttacker);
        }

        clearProjectiles(player);

        if (!leaving) setDeathMessage(player, attacker, source);
    }

    public void fulfilKill(@NotNull NexiaPlayer player, @Nullable DamageSource source, @Nullable NexiaPlayer attacker) {
        setInventory(attacker);
    }

    public void sendToSpawn(NexiaPlayer player) {
        player.getInventory().clear();
        wasInSpawn.add(player.getUUID());

        player.safeReset(true, getMinecraftGameMode());
        getSpawn().teleportPlayer(getNexusFfaWorld(), player);
        finishSendToSpawn(player);
    }

    public void respawn(NexiaPlayer player) {
        wasInSpawn.add(player.getUUID());
        finishSendToSpawn(player);
    }

    public void join(NexiaPlayer player, boolean tp) {
        if (tp) sendToSpawn(player);
    }

    public final void join(NexiaPlayer player) {
        join(player, true);
    }

    public Minecraft.GameMode getMinecraftGameMode() {
        return Minecraft.GameMode.ADVENTURE;
    }

    public abstract void finishSendToSpawn(NexiaPlayer player);

    public boolean beforeBuild(NexiaPlayer player, BlockPos blockPos) {
        return true;
    }
}
