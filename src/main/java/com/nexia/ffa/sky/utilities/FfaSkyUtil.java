package com.nexia.ffa.sky.utilities;

import com.combatreforged.factory.api.world.types.Minecraft;
import com.google.gson.Gson;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.sky.SkyFfaBlocks;
import com.nexia.ffa.sky.utilities.player.PlayerDataManager;
import com.nexia.ffa.sky.utilities.player.SavedPlayerData;
import io.github.blumbo.inventorymerger.InventoryMerger;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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

    public static boolean isFfaPlayer(NexiaPlayer player) {
        PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.hasTag("ffa_sky") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.SKY;
    }

    public static void fiveTick() {
        if(ffaWorld == null) return;
        if(ffaWorld.players().isEmpty()) return;
        for (ServerPlayer minecraftPlayer : ffaWorld.players()) {
            NexiaPlayer player = new NexiaPlayer(minecraftPlayer);
            if(wasInSpawn.contains(minecraftPlayer.getUUID()) && !isInFfaSpawn(player)){
                wasInSpawn.remove(minecraftPlayer.getUUID());
                minecraftPlayer.getCooldowns().addCooldown(Items.ENDER_PEARL, 10);
                saveInventory(player);
                player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            }
        }
    }

    public static void saveInventory(NexiaPlayer player){
        // /config/nexia/ffa/sky/inventory/savedInventories/uuid.json

        Inventory inventory = player.unwrap().inventory;

        ItemStack ogWoolItem = null;
        int ogWoolItemSlot = 36;

        for (int i = 0; i < 41; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getItem().toString().endsWith("_wool")) {
                ogWoolItemSlot = i;
                ogWoolItem = item;
                player.unwrap().inventory.setItem(i, new ItemStack(Items.WHITE_WOOL, 64));
            }
        }

        SavableInventory savableInventory = new SavableInventory(player.unwrap().inventory);
        String stringInventory = savableInventory.toSave();

        try {
            String file = dataDirectory + "/inventory/savedInventories/" + player.getUUID() + ".json";
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringInventory);
            fileWriter.close();
        } catch (Exception var6) {
            LobbyUtil.returnToLobby(player, true);
            player.sendMessage(Component.text("Failed to set Sky FFA inventory. Please try again or contact a developer.").color(ChatFormat.systemColor));
            return;
        }

        if(ogWoolItem != null) {
            player.unwrap().inventory.setItem(ogWoolItemSlot, ogWoolItem);
            player.refreshInventory();
            // problem solved
        }
    }

    public static void setInventory(NexiaPlayer player){

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
            player.sendMessage(Component.text("Failed to set Sky FFA inventory. Please try again or contact a developer.").color(ChatFormat.systemColor));
            return;
        }
        
        if(layout != null) {
            InventoryMerger.mergeSafe(player.unwrap(), layout.asPlayerInventory(), defaultInventory.asPlayerInventory());
        } else {
            player.unwrap().inventory.replaceWith(defaultInventory.asPlayerInventory());
        }

        for (int i = 0; i < 41; i++) {
            Item item = player.unwrap().inventory.getItem(i).getItem();
            if (item.toString().endsWith("_wool")) {
                ItemStack coloredWool = setWoolColor(new ItemStack(Items.WHITE_WOOL, 64));
                player.unwrap().inventory.setItem(i, coloredWool);
            }
        }

        player.refreshInventory();
    }

    public static void joinOrRespawn(NexiaPlayer player) {
        wasInSpawn.add(player.getUUID());
        player.reset(true, Minecraft.GameMode.SURVIVAL);
        player.unwrap().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000000, 1, true, false, false));
        setInventory(player);
    }

    private static ItemStack setWoolColor(ItemStack itemStack) {
        if (itemStack.getItem() != Items.WHITE_WOOL) return itemStack;

        if (woolId >= coloredWool.length) woolId = 0;
        itemStack = new ItemStack(coloredWool[woolId], itemStack.getCount());
        woolId++;
        return itemStack;
    }

    public static void clearEnderpearls(NexiaPlayer player) {
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


    public static void leaveOrDie(@NotNull NexiaPlayer player, @Nullable DamageSource source, boolean leaving) {

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player.unwrap());

        if(attacker != null) {
            NexiaPlayer nexiaPlayer = new NexiaPlayer(attacker);
            if(!nexiaPlayer.equals(player)) {
                SavedPlayerData data = PlayerDataManager.get(nexiaPlayer).savedData;

                data.killstreak++;
                if(data.killstreak > data.bestKillstreak){
                    data.bestKillstreak = data.killstreak;
                }
                data.kills++;
                FfaSkyUtil.killHeal(nexiaPlayer);
                FfaSkyUtil.giveKillLoot(nexiaPlayer, player);
            }
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

    public static void clearArrows(NexiaPlayer player) {
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

    public static void giveKillLoot(NexiaPlayer attacker, NexiaPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(attacker)) return;
        HashMap<Integer, ItemStack> availableRewards = (HashMap<Integer, ItemStack>) killRewards.clone();
        ArrayList<ItemStack> givenRewards = new ArrayList<>();


        for (int i = 0; i < Math.min(2, availableRewards.size()); i++) {
            // Pick reward
            int randomIndex = new Random().nextInt(availableRewards.size());
            int killRewardIndex = (Integer)availableRewards.keySet().toArray()[randomIndex];
            ItemStack reward = availableRewards.get(killRewardIndex);
            availableRewards.remove(killRewardIndex);

            // Give reward
            if (attacker.unwrap().inventory.contains(reward)) {
                attacker.unwrap().inventory.add(reward.copy());
            }
            givenRewards.add(reward.copy());
        }

        // Inform player about given rewards
        
        attacker.sendMessage(Component.text("[").color(ChatFormat.arrowColor)
                .append(Component.text("☠").color(ChatFormat.brandColor1))
                .append(Component.text("] ").color(ChatFormat.arrowColor))
                .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2))
        );

        for (ItemStack givenReward : givenRewards) {
            String itemName = LegacyChatFormat.removeColors(givenReward.getHoverName().getString());
            if (givenReward.getCount() > 1) itemName += "s";
            attacker.sendMessage(Component.text("[").color(ChatFormat.arrowColor)
                    .append(Component.text("+" + givenReward.getCount()).color(ChatFormat.brandColor1))
                    .append(Component.text("] ").color(ChatFormat.arrowColor))
                    .append(Component.text(itemName).color(ChatFormat.brandColor2))
            );
        }
        attacker.sendSound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 0.75f, 1f);
    }

    public static void killHeal(NexiaPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(player)) return;
        int minHeal = 4;
        int maxHeal = 11;
        float maxHealth = player.unwrap().getMaxHealth();
        float lostHearts = maxHealth - player.unwrap().getHealth();

        int heal = (int)(minHeal + (lostHearts - minHeal) * (maxHeal - minHeal) / (maxHealth - minHeal));

        if (player.unwrap().hasEffect(MobEffects.REGENERATION)) {
            player.unwrap().heal(heal);
        } else {
            player.unwrap().addEffect(new MobEffectInstance(MobEffects.REGENERATION, heal, 5, false, false));
        }
    }

    public static boolean canGoToSpawn(NexiaPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(player) || FfaSkyUtil.wasInSpawn.contains(player.getUUID())) return true;
        return !(Math.round(player.getHealth()) < 20);
    }

    public static void sendToSpawn(NexiaPlayer player) {
        FfaSkyUtil.clearArrows(player);
        FfaSkyUtil.clearEnderpearls(player);
        FfaSkyUtil.wasInSpawn.add(player.getUUID());

        player.reset(true, Minecraft.GameMode.SURVIVAL);
        spawn.teleportPlayer(ffaWorld, player.unwrap());
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

    public static boolean beforeBuild(NexiaPlayer player, BlockPos blockPos) {
        if (player.unwrap().isCreative()) return true;
        if (wasInSpawn.contains(player.getUUID()) || blockPos.getY() >= buildLimitY) {
            player.sendHandItemPacket();
            return false;
        }
        return canBuild(player, blockPos);
    }

    public static boolean beforeDamage(NexiaPlayer player, DamageSource damageSource) {
        if (damageSource == DamageSource.OUT_OF_WORLD) return true;

        return !isInFfaSpawn(player);
    }

    public static void afterPlace(NexiaPlayer player, BlockPos blockPos, InteractionHand hand) {
        if (!player.unwrap().isCreative()) {
            if (player.unwrap().getItemInHand(hand).getItem().toString().endsWith("_wool")) {
                player.unwrap().getItemInHand(hand).setCount(64);
                player.refreshInventory();
            }
            SkyFfaBlocks.placeBlock(blockPos);
        }
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
