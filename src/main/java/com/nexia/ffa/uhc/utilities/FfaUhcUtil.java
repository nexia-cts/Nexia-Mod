package com.nexia.ffa.uhc.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.google.gson.Gson;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
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
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

import static com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld;
import static com.nexia.ffa.uhc.utilities.FfaAreas.*;
import static com.nexia.ffa.uhc.utilities.player.PlayerDataManager.dataDirectory;

public class FfaUhcUtil {

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static HashMap<Integer, ItemStack> invItems;

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_uhc") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.UHC;
    }

    public static void ffaSecond() {
        if (ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
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

        BlfScheduler.delay(5, new BlfRunnable() {
            @Override
            public void run() {
                player.heal(player.getMaxHealth());
            }
        });

        FfaUhcUtil.clearArrows(player);
        FfaUhcUtil.clearTrident(player);

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
        if (ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !FfaAreas.isInFfaSpawn(minecraftPlayer)){
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                wasInSpawn.remove(minecraftPlayer.getUUID());
                saveInventory(minecraftPlayer);
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void saveInventory(ServerPlayer player){
        // /config/nexia/ffa/uhc/inventory/savedInventories/uuid.json

        SavableInventory savableInventory = new SavableInventory(player.inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = dataDirectory + "/inventory/savedInventories/" + player.getStringUUID() + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
            player.sendMessage(LegacyChatFormat.format("{f}Failed to save UHC FFA inventory. Please try again or contact a developer."), Util.NIL_UUID);
            return;
        }
    }

    public static void setInventory(ServerPlayer player){

        // /config/nexia/ffa/uhc/inventory/savedInventories/uuid.json
        // /config/nexia/ffa/uhc/inventory/default.json

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
            player.sendMessage(LegacyChatFormat.format("{f}Failed to set UHC FFA inventory. Please try again or contact a developer."), Util.NIL_UUID);
            return;
        }

        if(layout != null) {
            InventoryMerger.mergeSafe(player, layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        ItemStackUtil.sendInventoryRefreshPacket(player);
    }

    public static boolean beforeBuild(ServerPlayer player, BlockPos blockPos) {
        if (player.isCreative()) return true;
        if (wasInSpawn.contains(player.getUUID())) return false;
        return blockPos.getY() < FfaAreas.buildLimitY;
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

    public static void clearTrident(ServerPlayer player) {
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
            FfaUhcUtil.clearArrows(attacker);
            FfaUhcUtil.clearTrident(attacker);
            FfaUhcUtil.setInventory(attacker);
        }

        if(!leaving){
            FfaUhcUtil.setDeathMessage(player, source);
            FfaUhcUtil.sendToSpawn(player);
        }
    }

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaUhcUtil.isFfaPlayer(player) || FfaUhcUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    public static void setDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source) {
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(minecraftPlayer);

        calculateDeath(minecraftPlayer);

        Component msg = FfaUtil.returnDeathMessage(minecraftPlayer, source);

        if(attacker != null && msg.toString().contains("somehow killed themselves")  && attacker != minecraftPlayer) {

            Component component = FfaUtil.returnClassicDeathMessage(minecraftPlayer, attacker);
            if(component != null) msg = component;

            calculateKill(attacker);
        }

        for (Player player : ServerTime.factoryServer.getPlayers()) {
            if (player.hasTag("ffa_uhc")) player.sendMessage(msg);
        }
    }

    public static void sendToSpawn(ServerPlayer player) {
        player.inventory.clearContent();
        FfaUhcUtil.clearArrows(player);
        FfaUhcUtil.clearTrident(player);
        FfaUhcUtil.setInventory(player);
        FfaUhcUtil.wasInSpawn.add(player.getUUID());

        player.removeAllEffects();
        player.setGameMode(GameType.SURVIVAL);
        FfaAreas.spawn.teleportPlayer(FfaAreas.ffaWorld, player);

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

    static {
        invItems = new HashMap<>();

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 4);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(Enchantments.IMPALING, 2);

        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        axe.enchant(Enchantments.CLEAVING, 2);

        ItemStack lava_bucket = new ItemStack(Items.LAVA_BUCKET);
        ItemStack water_bucket = new ItemStack(Items.WATER_BUCKET);

        ItemStack cobblestone = new ItemStack(Items.COBBLESTONE);
        cobblestone.setCount(64);

        ItemStack oak_log = new ItemStack(Items.OAK_LOG);
        oak_log.setCount(64);

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(Enchantments.PIERCING, 1);
        crossbow.enchant(Enchantments.QUICK_CHARGE, 1);

        ItemStack shield = new ItemStack(Items.SHIELD);

        ItemStack golden_apples = new ItemStack(Items.GOLDEN_APPLE);
        golden_apples.setCount(13);

        ItemStack cobwebs = new ItemStack(Items.COBWEB);
        cobwebs.setCount(10);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 2);

        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(Enchantments.DIGGING_EFFICIENCY, 2);

        ItemStack arrows = new ItemStack(Items.ARROW);
        arrows.setCount(6);

        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);

        invItems.put(0, sword);
        invItems.put(1, trident);
        invItems.put(2, axe);
        invItems.put(3, lava_bucket);
        invItems.put(4, water_bucket);
        invItems.put(5, cobblestone);
        invItems.put(6, crossbow);
        invItems.put(7, cobwebs);
        invItems.put(8, golden_apples);

        invItems.put(30, lava_bucket);
        invItems.put(13, water_bucket);
        invItems.put(32, cobblestone);
        invItems.put(35, pickaxe);
        invItems.put(34, bow);

        invItems.put(21, lava_bucket);
        invItems.put(22, water_bucket);
        invItems.put(23, oak_log);

        invItems.put(31, water_bucket);
        invItems.put(17, arrows);
        invItems.put(16, shield);
        invItems.put(14, water_bucket);

        invItems.put(36, boots);
        invItems.put(37, leggings);
        invItems.put(38, chestplate);
        invItems.put(39, helmet);

        invItems.put(40, shield);
    }
}
