package com.nexia.ffa.uhc.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.uhc.FfaUhcBlocks;
import com.nexia.ffa.uhc.utilities.player.PlayerData;
import com.nexia.ffa.uhc.utilities.player.PlayerDataManager;
import com.nexia.ffa.uhc.utilities.player.SavedPlayerData;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.nexia.ffa.uhc.utilities.FfaAreas.*;

public class FfaUhcUtil {

    public static String ffaUhcDir = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/ffa/uhc";
    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static HashMap<Integer, ItemStack> invItems;

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_uhc") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.UHC;
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
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !FfaAreas.isInFfaSpawn(minecraftPlayer)){
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                wasInSpawn.remove(minecraftPlayer.getUUID());
                saveInventory(minecraftPlayer);
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void saveInventory(ServerPlayer minecraftPlayer){
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);
        Inventory newInv = playerData.ffaInventory = new Inventory(minecraftPlayer);

        for(int i = 0; i < newInv.getContainerSize(); ++i) {
            newInv.setItem(i, minecraftPlayer.inventory.getItem(i).copy());
        }
    }

    public static void setInventory(ServerPlayer player){
        HashMap<Integer, ItemStack> availableItems = (HashMap)invItems.clone();
        Inventory newInv = new Inventory(player);
        Inventory oldInv = PlayerDataManager.get(player).ffaInventory;
        int i;

        if (oldInv != null) {
            for(i = 0; i < oldInv.getContainerSize(); ++i) {
                Item item = oldInv.getItem(i).getItem();

                Iterator<Map.Entry<Integer, ItemStack>> it = availableItems.entrySet().iterator();

                while(it.hasNext()) {
                    Map.Entry<Integer, ItemStack> entry = it.next();
                    if (entry.getValue().getItem() == item) {
                        ItemStack itemStack = entry.getValue().copy();
                        newInv.setItem(i, itemStack);
                        it.remove();
                        break;
                    }
                }
            }
        }


        for (Map.Entry<Integer, ItemStack> integerItemStackEntry : availableItems.entrySet()) {
            ItemStack itemStack = integerItemStackEntry.getValue().copy();
            if (newInv.getItem(integerItemStackEntry.getKey()).isEmpty()) {
                newInv.setItem(integerItemStackEntry.getKey(), itemStack);
            } else {
                newInv.add(itemStack);
            }
        }

        for(i = 0; i < newInv.getContainerSize(); ++i) {
            ItemStack itemStack = newInv.getItem(i);
            if (itemStack == null) {
                itemStack = ItemStack.EMPTY;
            }

            player.inventory.setItem(i, itemStack);
        }

        ItemStackUtil.sendInventoryRefreshPacket(player);
    }

    public static boolean beforeBuild(ServerPlayer player, BlockPos blockPos) {
        if (player.isCreative()) return true;
        if (wasInSpawn.contains(player.getUUID())) return false;
        if(blockPos.getY() >= FfaAreas.buildLimitY) return false;
        return FfaAreas.canBuild(player, blockPos);
    }

    public static void afterPlace(ServerPlayer player, BlockPos blockPos, InteractionHand hand) {
        if (!player.isCreative()) FfaUhcBlocks.placeBlock(blockPos);
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
        ServerPlayer attacker = null;

        if (source != null && source.getEntity() != null && source.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            attacker = PlayerUtil.getPlayerAttacker(source.getEntity());
        }


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
        return !(player.getHealth() < 20);
    }

    public static void setDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source) {
        ServerPlayer attacker = null;
        Entity fAttacker = null;

        if (source != null && source.getEntity() != null) {
            fAttacker = source.getEntity();
            if(PlayerUtil.getPlayerAttacker(source.getEntity()) != null) attacker = PlayerUtil.getPlayerAttacker(source.getEntity());
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
    }

    static {
        invItems = new HashMap<>();

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 2);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(Enchantments.IMPALING, 1);

        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        axe.enchant(Enchantments.CLEAVING, 1);

        ItemStack lava_bucket = new ItemStack(Items.LAVA_BUCKET);
        ItemStack water_bucket = new ItemStack(Items.WATER_BUCKET);

        ItemStack cobblestone = new ItemStack(Items.COBBLESTONE);
        cobblestone.setCount(64);

        ItemStack oak_log = new ItemStack(Items.OAK_LOG);
        oak_log.setCount(64);

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(Enchantments.PIERCING, 1);

        ItemStack golden_apples = new ItemStack(Items.GOLDEN_APPLE);
        golden_apples.setCount(8);

        ItemStack cobwebs = new ItemStack(Items.COBWEB);
        cobwebs.setCount(10);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 1);

        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(Enchantments.DIGGING_EFFICIENCY, 1);

        ItemStack arrows = new ItemStack(Items.ARROW);
        arrows.setCount(8);

        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);

        invItems.put(0, sword);
        invItems.put(1, trident);
        invItems.put(2, axe);
        invItems.put(3, lava_bucket);
        invItems.put(4, water_bucket);
        invItems.put(5, cobblestone);
        invItems.put(6, crossbow);
        invItems.put(7, cobwebs);
        invItems.put(8, bow);

        invItems.put(30, lava_bucket);
        invItems.put(13, water_bucket);
        invItems.put(32, cobblestone);
        invItems.put(35, pickaxe);

        invItems.put(22, water_bucket);
        invItems.put(23, oak_log);

        invItems.put(31, water_bucket);
        invItems.put(17, arrows);

        invItems.put(36, boots);
        invItems.put(37, leggings);
        invItems.put(38, chestplate);
        invItems.put(39, helmet);

        invItems.put(40, golden_apples);
    }
}
