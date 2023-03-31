package com.nexia.ffa.utilities;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.player.PlayerData;
import com.nexia.ffa.utilities.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Predicate;

import static com.nexia.ffa.utilities.FfaAreas.*;

public class FfaUtil {
    public static ArrayList<UUID> wasInSpawn = new ArrayList();
    public static HashMap<Integer, ItemStack> invItems;

    public static boolean isFfaPlayer(Player player) {
        return PlayerUtil.hasTag((ServerPlayer) player, "ffa") || com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.FFA;
    }

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

    public static void fiveTick() {
        for (ServerPlayer player : ffaWorld.players()) {
            if(wasInSpawn.contains(player.getUUID()) && !FfaAreas.isInFfaSpawn(player)){
                wasInSpawn.remove(player.getUUID());
                saveInventory(player);
                PlayerUtil.sendActionbar(player, "§8Your inventory layout was saved.");
            }
        }
    }
    public static void saveInventory(Player player){
        PlayerData playerData = PlayerDataManager.get(player);
        Inventory newInv = playerData.FfaInventory = new Inventory(player);

        for(int i = 0; i < newInv.getContainerSize(); ++i) {
            newInv.setItem(i, player.inventory.getItem(i).copy());
        }
    }


    public static void leaveOrDie(ServerPlayer player, DamageSource damageSource) {
        player.inventory.setCarried(ItemStack.EMPTY);
        clearThrownTridents(player);

        if (damageSource != null) {
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker(damageSource.getEntity());
            if (attacker != null) {
                clearThrownTridents(attacker);
                setInventory(attacker);
            }
        }

        FfaUtil.setInventory(player);
        FfaUtil.wasInSpawn.add(player.getUUID());
    }

    private static boolean addFromOldInv(ServerPlayer player, ItemStack itemStack) {
        PlayerData playerData = PlayerDataManager.get(player);
        Inventory invLayout = playerData.FfaInventory;
        if (invLayout == null) return false;

        for (int i = 0; i < 41; i++) {
            Item item = invLayout.getItem(i).getItem();
            if (itemStack.getItem() == item && player.inventory.getItem(i).isEmpty()) {
                player.inventory.setItem(i, itemStack);
                return true;
            }
        }
        return false;
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
