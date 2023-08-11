package com.nexia.ffa.classic.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.player.PlayerData;
import com.nexia.ffa.classic.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.nexia.ffa.classic.utilities.FfaAreas.*;

public class FfaClassicUtil {
    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();
    public static HashMap<Integer, ItemStack> invItems;

    public static void ffaSecond() {
        if (ffaWorld == null) return;
        for (ServerPlayer player : ffaWorld.players()) {
            if (!isFfaPlayer(player)) continue;

            if (FfaAreas.isInFfaSpawn(player)) {
                player.addTag("no_damage");
            } else {
                player.removeTag("no_damage");
            }
        }
    }

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_classic") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.CLASSIC;
    }

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaClassicUtil.isFfaPlayer(player) || FfaClassicUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(player.getHealth() < 20);
    }

    public static void fiveTick() {
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !FfaAreas.isInFfaSpawn(minecraftPlayer)){
                wasInSpawn.remove(minecraftPlayer.getUUID());
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                saveInventory(minecraftPlayer);
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }
    public static void saveInventory(ServerPlayer minecraftPlayer){
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);
        Inventory newInv = playerData.FfaInventory = new Inventory(minecraftPlayer);

        for(int i = 0; i < newInv.getContainerSize(); ++i) {
            newInv.setItem(i, minecraftPlayer.inventory.getItem(i).copy());
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

        if(data.killstreak % 5 == 0){
            for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()){
                if(FfaClassicUtil.isFfaPlayer(serverPlayer)) {
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
    }

    /*
    public static float calculateHealth(float health){
        float fixedHealth = Float.parseFloat(new DecimalFormat("#.#").format(health / 2));

        if(fixedHealth < 0.5){
            return 0.5f;
        }
        if(fixedHealth >= 10){
            return 10f;
        }

        if(fixedHealth % 1 == 0){ return fixedHealth; }

        if(Float.parseFloat(new DecimalFormat("0.#").format(fixedHealth)) >= 0.5){
            return Float.parseFloat(new DecimalFormat("#.5").format(fixedHealth));
        }
        return Float.parseFloat(new DecimalFormat("#.0").format(fixedHealth));
    }

     */

    public static void calculateDeath(ServerPlayer player){
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


        if(attacker != null && msg == invalid && attacker != minecraftPlayer) {

            String symbol = "◆";
            Item handItem = attacker.getMainHandItem().getItem();

            if (handItem == Items.NETHERITE_SWORD) {
                symbol = "\uD83D\uDDE1";
            } else if (handItem == Items.TRIDENT) {
                symbol = "\uD83D\uDD31";
            } else if (handItem == Items.NETHERITE_AXE) {
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

    public static void setInventory(ServerPlayer player){
        HashMap<Integer, ItemStack> availableItems = (HashMap)invItems.clone();
        Inventory newInv = new Inventory(player);
        Inventory oldInv = PlayerDataManager.get(player).FfaInventory;
        int i;

        if (oldInv != null) {
            for(i = 0; i < 41; ++i) {
                Item item = oldInv.getItem(i).getItem();

                Iterator<Map.Entry<Integer, ItemStack>> it = availableItems.entrySet().iterator();

                while(it.hasNext()) {
                    Map.Entry<Integer, ItemStack> entry = (Map.Entry)it.next();
                    if (((ItemStack)entry.getValue()).getItem() == item) {
                        ItemStack itemStack = ((ItemStack)entry.getValue()).copy();
                        newInv.setItem(i, itemStack);
                        it.remove();
                        break;
                    }
                }
            }
        }

        for (Map.Entry<Integer, ItemStack> integerItemStackEntry : availableItems.entrySet()) {
            Map.Entry<Integer, ItemStack> entry = (Map.Entry) integerItemStackEntry;
            ItemStack itemStack = ((ItemStack) entry.getValue()).copy();
            if (newInv.getItem(entry.getKey()).isEmpty()) {
                newInv.setItem(entry.getKey(), itemStack);
            } else {
                newInv.add(itemStack);
            }
        }

        for(i = 0; i < 41; ++i) {
            ItemStack itemStack = newInv.getItem(i);
            if (itemStack == null) {
                itemStack = ItemStack.EMPTY;
            }

            player.inventory.setItem(i, itemStack);
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
        ServerPlayer attacker = null;

        if (source != null && source.getEntity() != null && source.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            attacker = PlayerUtil.getPlayerAttacker(source.getEntity());
        }

        if (attacker != null) {
            FfaClassicUtil.clearThrownTridents(attacker);
            FfaClassicUtil.setInventory(attacker);
        }

        if(!leaving){
            FfaClassicUtil.setDeathMessage(player, source);
        }

        FfaClassicUtil.setInventory(player);
        FfaClassicUtil.clearThrownTridents(player);
        FfaClassicUtil.wasInSpawn.add(player.getUUID());
    }

    static {
        invItems = new HashMap<>();
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(0, sword);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(1, trident);

        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        axe.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(2, axe);

        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);
        helmet.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(39, helmet);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);
        chestplate.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(38, chestplate);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);
        leggings.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(37, leggings);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);
        boots.getOrCreateTag().putBoolean("Unbreakable", true);
        invItems.put(36, boots);
    }
}
