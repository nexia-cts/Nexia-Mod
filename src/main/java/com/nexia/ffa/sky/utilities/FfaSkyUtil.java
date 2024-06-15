package com.nexia.ffa.sky.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.google.gson.Gson;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.sky.SkyFfaBlocks;
import com.nexia.ffa.sky.utilities.player.PlayerDataManager;
import com.nexia.ffa.sky.utilities.player.SavedPlayerData;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
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

import static com.nexia.ffa.sky.utilities.FfaAreas.*;
import static com.nexia.ffa.sky.utilities.player.PlayerDataManager.dataDirectory;

public class FfaSkyUtil {

    public static String ffaSkyDir = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/ffa/sky";

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static final Item[] coloredWool = {Items.RED_WOOL, Items.ORANGE_WOOL, Items.YELLOW_WOOL,
            Items.LIME_WOOL, Items.LIGHT_BLUE_WOOL, Items.MAGENTA_WOOL};
    public static int woolId = 0;
    public static final HashMap<Integer, ItemStack> killRewards = new HashMap<>();

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_sky") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.SKY;
    }

    public static void fiveTick() {
        if(ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !isInFfaSpawn(minecraftPlayer)){
                Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
                wasInSpawn.remove(minecraftPlayer.getUUID());
                minecraftPlayer.getCooldowns().addCooldown(Items.ENDER_PEARL, 10);
                saveInventory(minecraftPlayer);
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void saveInventory(ServerPlayer player){
        // /config/nexia/ffa/sky/inventory/savedInventories/uuid.json

        Inventory inventory = player.inventory;

        ItemStack ogWoolItem = null;
        int ogWoolItemSlot = 36;

        for (int i = 0; i < 41; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getItem().toString().endsWith("_wool")) {
                ogWoolItemSlot = i;
                ogWoolItem = item;
                player.inventory.setItem(i, new ItemStack(Items.WHITE_WOOL, 64));
            }
        }

        SavableInventory savableInventory = new SavableInventory(player.inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = dataDirectory + "/inventory/savedInventories/" + player.getStringUUID() + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
            player.sendMessage(LegacyChatFormat.format("{f}Failed to save Sky FFA inventory. Please try again or contact a developer."), Util.NIL_UUID);
            return;
        }

        if(ogWoolItem != null) {
            player.inventory.setItem(ogWoolItemSlot, ogWoolItem);
            ItemStackUtil.sendInventoryRefreshPacket(player);
            // problem solved
        }
    }

    public static void setInventory(ServerPlayer player){

        // /config/nexia/ffa/sky/inventory/savedInventories/uuid.json
        // /config/nexia/ffa/sky/inventory/default.json

        if (!isFfaPlayer(player)) return;

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
            player.sendMessage(LegacyChatFormat.format("{f}Failed to set Sky FFA inventory. Please try again or contact a developer."), Util.NIL_UUID);
            return;
        }
        
        if(layout != null) {
            InventoryMerger.mergeSafe(player, layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        for (int i = 0; i < 41; i++) {
            Item item = player.inventory.getItem(i).getItem();
            if (item.toString().endsWith("_wool")) {
                ItemStack coloredWool = setWoolColor(new ItemStack(Items.WHITE_WOOL, 64));
                player.inventory.setItem(i, coloredWool);
            }
        }

        ItemStackUtil.sendInventoryRefreshPacket(player);
    }

    public static void joinOrRespawn(ServerPlayer player) {
        PlayerUtil.resetHealthStatus(player);
        wasInSpawn.add(player.getUUID());
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000000, 1, true, false, false));
        player.setGameMode(GameType.SURVIVAL);
        setInventory(player);
    }

    private static ItemStack setWoolColor(ItemStack itemStack) {
        if (itemStack.getItem() != Items.WHITE_WOOL) return itemStack;

        if (woolId >= coloredWool.length) woolId = 0;
        itemStack = new ItemStack(coloredWool[woolId], itemStack.getCount());
        woolId++;
        return itemStack;
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

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player);

        if(attacker != null && attacker != player) {
            SavedPlayerData data = PlayerDataManager.get(attacker).savedData;

            data.killstreak++;
            if(data.killstreak > data.bestKillstreak){
                data.bestKillstreak = data.killstreak;
            }
            data.kills++;
            FfaSkyUtil.killHeal(attacker);
            FfaSkyUtil.giveKillLoot(attacker, player);
        }

        FfaSkyUtil.clearEnderpearls(player);
        FfaSkyUtil.clearArrows(player);

        SavedPlayerData data = PlayerDataManager.get(player).savedData;
        data.deaths++;
        if(data.killstreak > data.bestKillstreak){
            data.bestKillstreak = data.killstreak;
        }
        data.killstreak = 0;

        if(!leaving){
            FfaSkyUtil.sendToSpawn(player);
        }
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

    public static void giveKillLoot(ServerPlayer attacker, ServerPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(attacker)) return;
        HashMap<Integer, ItemStack> availableRewards = (HashMap<Integer, ItemStack>) killRewards.clone();
        ArrayList<ItemStack> givenRewards = new ArrayList<>();

        for (int i = 0; i < Math.min(2, availableRewards.size()); i++) {
            // Pick reward
            int randomIndex = attacker.getRandom().nextInt(availableRewards.size());
            int killRewardIndex = (Integer)availableRewards.keySet().toArray()[randomIndex];
            ItemStack reward = availableRewards.get(killRewardIndex);
            availableRewards.remove(killRewardIndex);

            // Give reward
            attacker.inventory.add(reward.copy());
            givenRewards.add(reward.copy());
        }

        // Inform player about given rewards
        
        PlayerUtil.getFactoryPlayer(attacker).sendMessage(Component.text("[").color(ChatFormat.arrowColor)
                .append(Component.text("☠").color(ChatFormat.brandColor1))
                .append(Component.text("] ").color(ChatFormat.arrowColor))
                .append(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2))
        );

        for (ItemStack givenReward : givenRewards) {
            String itemName = LegacyChatFormat.removeColors(givenReward.getHoverName().getString());
            if (givenReward.getCount() > 1) itemName += "s";
            PlayerUtil.getFactoryPlayer(attacker).sendMessage(Component.text("[").color(ChatFormat.arrowColor)
                    .append(Component.text("+" + givenReward.getCount()).color(ChatFormat.brandColor1))
                    .append(Component.text("] ").color(ChatFormat.arrowColor))
                    .append(Component.text(itemName).color(ChatFormat.brandColor2))
            );
        }
        PlayerUtil.sendSound(attacker, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 0.75f, 1f);
    }

    public static void killHeal(ServerPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(player)) return;
        int minHeal = 4;
        int maxHeal = 11;
        float maxHealth = player.getMaxHealth();
        float lostHearts = maxHealth - player.getHealth();

        int heal = (int)(minHeal + (lostHearts - minHeal) * (maxHeal - minHeal) / (maxHealth - minHeal));

        if (player.hasEffect(MobEffects.REGENERATION)) {
            player.heal(heal);
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, heal, 5, false, false));
        }
    }

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(player) || FfaSkyUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    public static void sendToSpawn(ServerPlayer player) {
        player.inventory.clearContent();
        FfaSkyUtil.clearArrows(player);
        FfaSkyUtil.clearEnderpearls(player);
        player.removeAllEffects();
        FfaSkyUtil.wasInSpawn.add(player.getUUID());

        player.setGameMode(GameType.ADVENTURE);
        spawn.teleportPlayer(ffaWorld, player);
        FfaSkyUtil.setInventory(player);
    }

    private static ItemStack gApplePotion() {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.setHoverName(new TextComponent("\247ePiss Juice\2477™"));
        potion.getOrCreateTag().putInt("CustomPotionColor", 16771584);

        ArrayList<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        effects.add(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        PotionUtils.setCustomEffects(potion, effects);

        return potion;
    }

    public static boolean beforeBuild(ServerPlayer player, BlockPos blockPos) {
        if (player.isCreative()) return true;
        if (wasInSpawn.contains(player.getUUID()) || blockPos.getY() >= buildLimitY) {
            InventoryUtil.sendHandItemPacket(player, player.getUsedItemHand());
            return false;
        }
        return canBuild(player, blockPos);
    }

    public static boolean beforeDamage(ServerPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }

    public static void afterPlace(ServerPlayer player, BlockPos blockPos, InteractionHand hand) {
        if (!player.isCreative()) {
            if (player.getItemInHand(hand).getItem().toString().endsWith("_wool")) {
                player.getItemInHand(hand).setCount(64);
                ItemStackUtil.sendInventoryRefreshPacket(player);
            }
            SkyFfaBlocks.placeBlock(blockPos);
        }
    }

    private static ItemStack shears() {
        ItemStack shears = new ItemStack(Items.SHEARS);
        shears.enchant(Enchantments.DIGGING_EFFICIENCY, 4);
        return shears;
    }

    static {
        ItemStack pearl = new ItemStack(Items.ENDER_PEARL, 1);
        ItemStack cobweb = new ItemStack(Items.COBWEB, 2);
        ItemStack arrow = new ItemStack(Items.ARROW, 4);
        pearl.getOrCreateTag();
        cobweb.getOrCreateTag();
        arrow.getOrCreateTag();

        killRewards.put(3, gApplePotion());
        killRewards.put(4, pearl);
        killRewards.put(5, cobweb);
        killRewards.put(9, arrow);
    }
}
