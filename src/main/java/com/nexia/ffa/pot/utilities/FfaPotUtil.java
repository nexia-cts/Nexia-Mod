package com.nexia.ffa.pot.utilities;

import com.combatreforged.metis.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.pot.utilities.player.PlayerData;
import com.nexia.ffa.pot.utilities.player.PlayerDataManager;
import com.nexia.ffa.pot.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.nexia.ffa.pot.utilities.FfaAreas.*;

public class FfaPotUtil {

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static HashMap<Integer, ItemStack> invItems;

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_pot") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.POT;
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

        FfaPotUtil.clearExperience(player, true);
        FfaPotUtil.clearEnderpearls(player);

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
        Inventory oldInv = com.nexia.ffa.uhc.utilities.player.PlayerDataManager.get(player).ffaInventory;
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

    public static void clearExperience(ServerPlayer player, boolean clear) {
        BlockPos c1 = ffaCorner1.offset(-10, -ffaCorner1.getY(), -10);
        BlockPos c2 = ffaCorner2.offset(10, 319 - ffaCorner2.getY(), 10);
        AABB aabb = new AABB(c1, c2);
        Predicate<Entity> predicate = o -> true;
        for (ThrownExperienceBottle bottle : ffaWorld.getEntities(EntityType.EXPERIENCE_BOTTLE, aabb, predicate)) {
            if (bottle.getOwner() != null && bottle.getOwner().getUUID().equals(player.getUUID())) {
                bottle.remove();
            }
        }
        for (ExperienceOrb orb : ffaWorld.getEntities(EntityType.EXPERIENCE_ORB, aabb, predicate)) {
            orb.remove();
        }
        if(clear) player.setExperiencePoints(0);
    }

    public static void clearEnderpearls(ServerPlayer player) {
        BlockPos c1 = ffaCorner1.offset(-10, -ffaCorner1.getY(), -10);
        BlockPos c2 = ffaCorner2.offset(10, 319 - ffaCorner2.getY(), 10);
        AABB aabb = new AABB(c1, c2);
        Predicate<Entity> predicate = o -> true;
        for (ThrownEnderpearl pearl : ffaWorld.getEntities(EntityType.ENDER_PEARL, aabb, predicate)) {
            if (pearl.getOwner() != null && pearl.getOwner().getUUID().equals(player.getUUID())) {
                pearl.remove();
            }
        }
    }


    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        ServerPlayer attacker = null;

        if (source != null && source.getEntity() != null && source.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            attacker = PlayerUtil.getPlayerAttacker(source.getEntity());
        }


        if (attacker != null) {
            FfaPotUtil.clearEnderpearls(attacker);
            FfaPotUtil.clearExperience(attacker, true);
            FfaPotUtil.setInventory(attacker);
            attacker.removeAllEffects();
        }

        if(!leaving){
            FfaPotUtil.setDeathMessage(player, source);
            FfaPotUtil.sendToSpawn(player);
        }


    }

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaPotUtil.isFfaPlayer(player) || FfaPotUtil.wasInSpawn.contains(player.getUUID())) return true;
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
            }

            msg = Component.text("☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" was killed by ").color(ChatFormat.chatColor2))
                    .append(Component.text(symbol + " " + attacker.getScoreboardName()).color(ChatFormat.greenColor))
                    .append(Component.text(" with ").color(ChatFormat.chatColor2))
                    .append(Component.text(FfaUtil.calculateHealth(attacker.getHealth()) + "❤").color(ChatFormat.failColor))
                    .append(Component.text(" left.").color(ChatFormat.chatColor2));
        }

         if(attacker != null) calculateKill(attacker);

        for (Player player : ServerTime.metisServer.getPlayers()) {
            if (player.hasTag("ffa_pot")) player.sendMessage(msg);
        }
    }

    public static void sendToSpawn(ServerPlayer player) {
        player.inventory.clearContent();
        FfaPotUtil.clearExperience(player, true);
        FfaPotUtil.clearEnderpearls(player);
        player.removeAllEffects();
        FfaPotUtil.wasInSpawn.add(player.getUUID());

        player.setGameMode(GameType.ADVENTURE);
        FfaAreas.spawn.teleportPlayer(FfaAreas.ffaWorld, player);
        FfaPotUtil.setInventory(player);
    }

    static {
        invItems = new HashMap<>();

        ItemStack healing_potion = new ItemStack(Items.SPLASH_POTION);
        healing_potion.setHoverName(LegacyChatFormat.format("\247fSplash Potion of Healing"));
        healing_potion.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(Potions.STRONG_HEALING));
        PotionUtils.setPotion(healing_potion, Potions.STRONG_HEALING);

        // r * 65536 + g * 256 + b;

        ItemStack strength_splash_potion = new ItemStack(Items.SPLASH_POTION);
        strength_splash_potion.setHoverName(LegacyChatFormat.format("\247fSplash Potion of Strength"));
        strength_splash_potion.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(Potions.STRONG_STRENGTH));
        PotionUtils.setPotion(strength_splash_potion, Potions.STRONG_STRENGTH);

        ItemStack speed_splash_potion = new ItemStack(Items.SPLASH_POTION);
        speed_splash_potion.setHoverName(LegacyChatFormat.format("\247fSplash Potion of Swiftness"));
        speed_splash_potion.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(Potions.STRONG_SWIFTNESS));
        PotionUtils.setPotion(speed_splash_potion, Potions.STRONG_SWIFTNESS);


        ItemStack strength_potion = new ItemStack(Items.POTION);
        strength_potion.setHoverName(LegacyChatFormat.format("\247fPotion of Strength"));
        strength_potion.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(Potions.STRONG_STRENGTH));
        PotionUtils.setPotion(strength_potion, Potions.STRONG_STRENGTH);

        ItemStack speed_potion = new ItemStack(Items.POTION);

        speed_potion.setHoverName(LegacyChatFormat.format("\247fPotion of Swiftness"));
        speed_potion.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(Potions.STRONG_SWIFTNESS));
        PotionUtils.setPotion(speed_potion, Potions.STRONG_SWIFTNESS);

        strength_potion.setCount(16);
        speed_potion.setCount(16);

        ItemStack experience_bottles = new ItemStack(Items.EXPERIENCE_BOTTLE);
        experience_bottles.setCount(64);

        for (int i = 0; i < 35; i++) {
            invItems.put(i, healing_potion);
        }

        ItemStack goldenApple = new ItemStack(Items.GOLDEN_APPLE);
        goldenApple.setCount(64);

        ItemStack no_kb = new ItemStack(Items.NETHERITE_SWORD);
        no_kb.enchant(Enchantments.SHARPNESS, 5);
        no_kb.enchant(Enchantments.SWEEPING_EDGE, 3);
        no_kb.enchant(Enchantments.UNBREAKING, 3);
        no_kb.enchant(Enchantments.MOB_LOOTING, 3);
        no_kb.enchant(Enchantments.MENDING, 1);

        ItemStack kb = new ItemStack(Items.NETHERITE_SWORD);
        kb.enchant(Enchantments.SHARPNESS, 5);
        kb.enchant(Enchantments.SWEEPING_EDGE, 3);
        kb.enchant(Enchantments.UNBREAKING, 3);
        kb.enchant(Enchantments.MOB_LOOTING, 3);
        kb.enchant(Enchantments.MENDING, 1);
        kb.enchant(Enchantments.KNOCKBACK, 1);

        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);
        helmet.enchant(Enchantments.UNBREAKING, 3);
        helmet.enchant(Enchantments.MENDING, 1);

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        chestplate.enchant(Enchantments.UNBREAKING, 3);
        chestplate.enchant(Enchantments.MENDING, 1);

        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        leggings.enchant(Enchantments.UNBREAKING, 3);
        leggings.enchant(Enchantments.MENDING, 1);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);
        boots.enchant(Enchantments.UNBREAKING, 3);
        boots.enchant(Enchantments.FALL_PROTECTION, 4);
        boots.enchant(Enchantments.DEPTH_STRIDER, 3);
        boots.enchant(Enchantments.MENDING, 1);




        invItems.put(9, no_kb);
        invItems.put(1, goldenApple);

        invItems.put(7, strength_potion);
        invItems.put(8, speed_potion);

        invItems.put(34, strength_splash_potion);
        invItems.put(35, speed_splash_potion);

        invItems.put(25, strength_splash_potion);
        invItems.put(26, speed_splash_potion);

        invItems.put(16, experience_bottles);
        invItems.put(17, experience_bottles);


        invItems.put(36, boots);
        invItems.put(37, leggings);
        invItems.put(38, chestplate);
        invItems.put(39, helmet);

        invItems.put(40, new ItemStack(Items.TOTEM_OF_UNDYING));

        invItems.put(0, kb);
    }
}
