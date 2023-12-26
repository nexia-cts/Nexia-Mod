package com.nexia.ffa.sky.utilities;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.SkyFfaBlocks;
import com.nexia.ffa.sky.utilities.player.PlayerData;
import com.nexia.ffa.sky.utilities.player.PlayerDataManager;
import com.nexia.ffa.sky.utilities.player.SavedPlayerData;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.nexia.ffa.sky.utilities.FfaAreas.*;

public class FfaSkyUtil {

    public static String ffaSkyDir = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/ffa/sky";
    public static HashMap<UUID, Integer> fallInvulnerable = new HashMap<>();

    public static ArrayList<UUID> wasInSpawn = new ArrayList<>();

    public static final Item[] coloredWool = {Items.RED_WOOL, Items.ORANGE_WOOL, Items.YELLOW_WOOL,
            Items.LIME_WOOL, Items.LIGHT_BLUE_WOOL, Items.MAGENTA_WOOL};
    public static int woolId = 0;
    public static final HashMap<Integer, ItemStack> killRewards = new HashMap<>();
    public static HashMap<Integer, ItemStack> invItems;

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        com.nexia.core.utilities.player.PlayerData data = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        return player.getTags().contains("ffa_sky") && data.gameMode == PlayerGameMode.FFA && data.ffaGameMode == FfaGameMode.SKY;
    }

    public static void join(ServerPlayer player) {
        ServerLevel spawnWorld = ffaWorld;
        EntityPos spawn = FfaAreas.spawn;
        player.setRespawnPosition(spawnWorld.dimension(), spawn.toBlockPos(), spawn.yaw, true, false);
        spawn.teleportPlayer(spawnWorld, player);

        joinOrRespawn(player);
    }

    public static void ffaSecond() {
        Iterator<UUID> it = fallInvulnerable.keySet().iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            Integer time = fallInvulnerable.get(uuid);
            time--;
            if (time <= 0) it.remove();
            else fallInvulnerable.put(uuid, time);
        }
        for (ServerPlayer player : ffaWorld.players()) {
            if (FfaAreas.isInFfaSpawn(player)) {
                fallInvulnerable.put(player.getUUID(), 4);
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

        FfaSkyUtil.clearExperience(player, true);
        FfaSkyUtil.clearEnderpearls(player);

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
                minecraftPlayer.getCooldowns().addCooldown(Items.ENDER_PEARL, 10);
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
        HashMap<Integer, ItemStack> availableItems = (HashMap<Integer, ItemStack>)invItems.clone();
        Inventory newInv = new Inventory(player);
        Inventory oldInv = PlayerDataManager.get(player).ffaInventory;

        if (oldInv != null) {
            // Attempt to match new given items with previous inventory layout
            for (int i = 0; i < 41; i++) {
                Item item = oldInv.getItem(i).getItem();
                if (item.toString().endsWith("_wool")) item = Items.WHITE_WOOL;
                for (Iterator<Map.Entry<Integer, ItemStack>> it = availableItems.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, ItemStack> entry = it.next();
                    if (entry.getValue().getItem() == item) {
                        ItemStack itemStack = setWoolColor(entry.getValue().copy());
                        newInv.setItem(i, itemStack);
                        it.remove();
                        break;
                    }
                }
            }
        }


        // Add items that could not be matched
        for (Map.Entry<Integer, ItemStack> entry : availableItems.entrySet()) {
            ItemStack itemStack = setWoolColor(entry.getValue().copy());
            if (newInv.getItem(entry.getKey()).isEmpty()) {
                newInv.setItem(entry.getKey(), itemStack);
            } else {
                newInv.add(itemStack);
            }
        }
        // Give player the items
        for (int i = 0; i < 41; i++) {
            ItemStack itemStack = newInv.getItem(i);
            if (itemStack == null) itemStack = ItemStack.EMPTY;
            player.inventory.setItem(i, itemStack);
        }

        ItemStackUtil.sendInventoryRefreshPacket(player);
    }

    public static void joinOrRespawn(ServerPlayer player) {
        PlayerUtil.resetHealthStatus(player);
        fallInvulnerable.put(player.getUUID(), 4);
        wasInSpawn.add(player.getUUID());
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000000, 0, true, false, false));
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
            FfaSkyUtil.killHeal(attacker);
            FfaSkyUtil.giveKillLoot(attacker);
            FfaSkyUtil.clearEnderpearls(player);
            FfaKitsUtil.clearArrows(player);
        }

        if(!leaving){
            FfaSkyUtil.setDeathMessage(player, source);
            FfaSkyUtil.sendToSpawn(player);
        }


    }

    public static void giveKillLoot(ServerPlayer player) {
        HashMap<Integer, ItemStack> availableRewards = (HashMap<Integer, ItemStack>) killRewards.clone();
        ArrayList<ItemStack> givenRewards = new ArrayList<>();


        for (int i = 0; i < Math.min(2, availableRewards.size()); i++) {
            // Pick reward
            int randomIndex = player.getRandom().nextInt(availableRewards.size());
            int killRewardIndex = (Integer)availableRewards.keySet().toArray()[randomIndex];
            ItemStack reward = availableRewards.get(killRewardIndex);
            availableRewards.remove(killRewardIndex);

            // Give reward
            if (player.inventory.contains(reward) || !addFromOldInv(player, reward.copy())) {
                player.inventory.add(reward.copy());
            }
            givenRewards.add(reward.copy());
        }

        // Inform player about given rewards
        for (ItemStack givenReward : givenRewards) {
            String itemName = LegacyChatFormat.removeColors(givenReward.getHoverName().getString());
            if (givenReward.getCount() > 1) itemName += "s";
            player.sendMessage(LegacyChatFormat.format("\247a+{} \247e{}", givenReward.getCount(), itemName), Util.NIL_UUID);
        }
        PlayerUtil.sendSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 0.75f, 1f);
    }

    public static void killHeal(ServerPlayer player) {
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


    private static boolean addFromOldInv(ServerPlayer player, ItemStack itemStack) {
        PlayerData playerData = PlayerDataManager.get(player);
        Inventory invLayout = playerData.ffaInventory;
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

    public static boolean canGoToSpawn(ServerPlayer player) {
        if(!FfaSkyUtil.isFfaPlayer(player) || FfaSkyUtil.wasInSpawn.contains(player.getUUID())) return true;
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

        for (Player player : ServerTime.factoryServer.getPlayers()) {
            if (player.hasTag("ffa_sky")) player.sendMessage(msg);
        }
    }

    public static void sendToSpawn(ServerPlayer player) {
        player.inventory.clearContent();
        FfaSkyUtil.clearExperience(player, true);
        FfaSkyUtil.clearEnderpearls(player);
        player.removeAllEffects();
        FfaSkyUtil.wasInSpawn.add(player.getUUID());

        player.setGameMode(GameType.ADVENTURE);
        FfaAreas.spawn.teleportPlayer(FfaAreas.ffaWorld, player);
        FfaSkyUtil.setInventory(player);
    }

    static {
        invItems = new HashMap<>();

        invItems.put(0, new ItemStack(Items.IRON_SWORD));
        invItems.put(1, new ItemStack(Items.WHITE_WOOL, 64));
        invItems.put(2, new ItemStack(Items.BOW));
        killRewards.put(3, gApplePotion());
        killRewards.put(4, new ItemStack(Items.ENDER_PEARL, 1));
        killRewards.put(5, new ItemStack(Items.COBWEB, 2));
        invItems.put(8, shears());

        killRewards.put(9, new ItemStack(Items.ARROW, 4));

        for (Map.Entry<Integer, ItemStack> killReward : killRewards.entrySet()) {
            invItems.put(killReward.getKey(), killReward.getValue().copy());
        }

        for (ItemStack itemStack : invItems.values()) {
            if (itemStack.isDamageableItem()) itemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        }
    }

    private static ItemStack gApplePotion() {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.setHoverName(new TextComponent("\247bPiss Juice²"));
        potion.getOrCreateTag().putInt("CustomPotionColor", 16771584);

        ArrayList<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(MobEffects.REGENERATION, 80, 2));
        effects.add(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        PotionUtils.setCustomEffects(potion, effects);

        return potion;
    }

    public static boolean beforeBuild(ServerPlayer player, BlockPos blockPos) {
        if (player.isCreative()) return true;
        return FfaAreas.canBuild(player, blockPos);
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
}
