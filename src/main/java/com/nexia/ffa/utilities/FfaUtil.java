package com.nexia.ffa.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.player.PlayerData;
import com.nexia.ffa.utilities.player.PlayerDataManager;
import com.nexia.ffa.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;

import static com.nexia.ffa.utilities.FfaAreas.*;

public class FfaUtil {
    public static ArrayList<UUID> wasInSpawn = new ArrayList();
    public static HashMap<Integer, ItemStack> invItems;

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        return player.getTags().contains("ffa") && com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.FFA;
    }

    public static void ffaSecond() {
        if (ffaWorld == null) return;
        for (ServerPlayer player : ffaWorld.players()) {
            if (!isFfaPlayer(player)) continue;

            if (FfaAreas.isInFfaSpawn(player)) {
                player.addTag(LobbyUtil.NO_DAMAGE_TAG);
            } else {
                player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            }
        }
    }

    public static void fiveTick() {
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !FfaAreas.isInFfaSpawn(minecraftPlayer)){
                wasInSpawn.remove(minecraftPlayer.getUUID());
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                saveInventory(minecraftPlayer);
                player.sendActionBarMessage(ChatFormat.nexiaMessage().append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
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
            for(int i = 0; i < Main.server.getPlayerCount(); i++){
                if(FfaUtil.isFfaPlayer(PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]))){
                    PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(LegacyChatFormat.format("§8[§c☠§8] §a{n}{} {s}now has a killstreak of §c{b}{}{s}!", player.getScoreboardName(), data.killstreak), Util.NIL_UUID);
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

    public static float calculateHealth(float health){
        float fixedHealth = Float.parseFloat(new DecimalFormat("#.#").format(health / 2));

        if(fixedHealth <= 0){
            return 0.5f;
        }
        if(fixedHealth >= 10){
            return 10f;
        }

        if(!(fixedHealth % 1 == 0)){ return fixedHealth; }

        if(Float.parseFloat(new DecimalFormat("#.5").format(fixedHealth)) >= 10.5){
            return 10f;
        }
        if(((fixedHealth / 2) % 1) >= .5){
            return Float.parseFloat(new DecimalFormat("#.5").format(fixedHealth));
        }
        return Float.parseFloat(new DecimalFormat("#.0").format(fixedHealth));
    }

    public static void calculateDeath(ServerPlayer player){
        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }

        if(data.killstreak >= 5) {
            for (int i = 0; i < Main.server.getPlayerCount(); i++) {
                if (FfaUtil.isFfaPlayer(PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]))) {
                    PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(LegacyChatFormat.format("§8[§c☠§8] §c{n}{} {s}has lost their killstreak of §c{b}{}{s}.", player.getScoreboardName(), data.killstreak), Util.NIL_UUID);
                }
            }
        }
        data.killstreak = 0;
    }

    public static void setDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source){
        boolean attackerNull = source == null || !(source.getEntity() instanceof ServerPlayer);
        boolean victimTag = FfaUtil.isFfaPlayer(minecraftPlayer);

        if((attackerNull && victimTag) || (!attackerNull && source.getEntity() == minecraftPlayer && victimTag)){
            for(int i = 0; i < Main.server.getPlayerCount(); i++){
                if(FfaUtil.isFfaPlayer(PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]))){

                    TextComponent msg = LegacyChatFormat.format("§7Wow, §c☠ {} §7somehow killed themselves.", minecraftPlayer.getScoreboardName());

                    if(source == DamageSource.OUT_OF_WORLD) {
                        msg = LegacyChatFormat.format("§c⚐ {} §7took a ride to the void.", minecraftPlayer.getScoreboardName());
                    }

                    if(source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
                        msg = LegacyChatFormat.format("§c\uD83D\uDD25 {} §7was deepfried in lava.", minecraftPlayer.getScoreboardName());
                    }

                    if(source == DamageSource.DROWN) {
                        msg = LegacyChatFormat.format("§c\uD83C\uDF0A {} §7forgot to breathe air.", minecraftPlayer.getScoreboardName());
                    }

                    if(source == DamageSource.HOT_FLOOR) {
                        msg = LegacyChatFormat.format("§c\uD83D\uDD25 {} §7stepped on hot legos.", minecraftPlayer.getScoreboardName());
                    }

                    if(source == DamageSource.CACTUS) {
                        msg = LegacyChatFormat.format("§7ʕっ·ᴥ·ʔっ §c☠ {} §7hugged a cactus.", minecraftPlayer.getScoreboardName());
                    }

                    PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(msg, Util.NIL_UUID);
                }
            }
            return;
        }
        if(victimTag){
            calculateDeath(minecraftPlayer);
        }
        if(attackerNull) { return; }
        ServerPlayer minecraftAttacker = (ServerPlayer) source.getEntity();
        boolean attackerTag = FfaUtil.isFfaPlayer(minecraftAttacker);

        if(attackerTag && victimTag){
            String symbol = "◆";
            Item handItem = minecraftAttacker.getMainHandItem().getItem();

            if(handItem == Items.NETHERITE_SWORD){
                symbol = "\uD83D\uDDE1";
            } else if(handItem == Items.TRIDENT){
                symbol = "\uD83D\uDD31";
            } else if(handItem == Items.NETHERITE_AXE) {
                symbol = "\uD83E\uDE93";
            }
            for(int i = 0; i < Main.server.getPlayerCount(); i++){
                if(FfaUtil.isFfaPlayer(PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]))){
                    PlayerUtil.getMinecraftPlayerFromName(Main.server.getPlayerNames()[i]).sendMessage(new TextComponent("§c☠ " + minecraftPlayer.getScoreboardName() + " §7was killed by §a" + symbol + " " +  minecraftAttacker.getScoreboardName() + " §7with §c" + calculateHealth(minecraftAttacker.getHealth()) + "❤ §7left."), Util.NIL_UUID);
                }
            }
        }

        if(attackerTag){
            calculateKill(minecraftAttacker);
        }
    }


    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        player.inventory.setCarried(ItemStack.EMPTY);
        clearThrownTridents(player);

        ServerPlayer attacker = null;

        if (source != null && source.getEntity() != null && source.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            attacker = PlayerUtil.getPlayerAttacker(source.getEntity());
        }

        if (attacker != null) {
            clearThrownTridents(attacker);
            setInventory(attacker);
        }

        if(!leaving){
            setDeathMessage(player, source);
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
