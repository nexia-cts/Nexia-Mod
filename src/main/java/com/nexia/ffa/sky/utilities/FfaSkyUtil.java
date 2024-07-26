package com.nexia.ffa.sky.utilities;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.ffa.sky.SkyFfaBlocks;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.nexia.ffa.sky.utilities.SkyFfaAreas.buildLimitY;
import static com.nexia.ffa.sky.utilities.SkyFfaAreas.canBuild;

public class FfaSkyUtil extends BaseFfaUtil {

    public static String ffaSkyDir = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/ffa/sky";

    public static final FfaSkyUtil INSTANCE = new FfaSkyUtil();

    public static final Item[] coloredWool = {Items.RED_WOOL, Items.ORANGE_WOOL, Items.YELLOW_WOOL,
            Items.LIME_WOOL, Items.LIGHT_BLUE_WOOL, Items.MAGENTA_WOOL};
    public static int woolId = 0;
    public static final HashMap<Integer, ItemStack> killRewards = new HashMap<>();

    public FfaSkyUtil() {
        super(new SkyFfaAreas());
    }

    @Override
    public String getName() {
        return "Sky";
    }

    @Override
    public FfaGameMode getGameMode() {
        return FfaGameMode.SKY;
    }

    @Override
    public PlayerDataManager getDataManager() {
        return PlayerDataManager.getDataManager(NexiaCore.FFA_SKY_DATA_MANAGER);
    }

    @Override
    public void completeFiveTick(NexiaPlayer player) {
        if(wasInSpawn.contains(player.getUUID()) && !isInFfaSpawn(player)){
            wasInSpawn.remove(player.getUUID());
            player.unwrap().getCooldowns().addCooldown(Items.ENDER_PEARL, 10);
            saveInventory(player);
            player.sendActionBarMessage(ChatFormat.nexiaMessage.append(Component.text("Your inventory layout was saved.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        }
    }

    public void saveInventory(NexiaPlayer player) {
        Inventory inventory = player.unwrap().inventory;

        ItemStack ogWoolItem = null;
        int ogWoolItemSlot = 36;

        for (int i = 0; i < 41; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getItem().toString().endsWith("_wool")) {
                ogWoolItemSlot = i;
                ogWoolItem = item;
                player.getInventory().setItemStack(i, com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.WHITE_WOOL, 64));
            }
        }

        super.saveInventory(player);

        if(ogWoolItem != null) {
            player.unwrap().inventory.setItem(ogWoolItemSlot, ogWoolItem);
            player.refreshInventory();
        }
    }

    @Override
    public void alterInventory(NexiaPlayer player) {
        for (int i = 0; i < 41; i++) {
            Item item = player.unwrap().inventory.getItem(i).getItem();
            if (item.toString().endsWith("_wool")) {
                ItemStack coloredWool = setWoolColor(new ItemStack(Items.WHITE_WOOL, 64));
                player.unwrap().inventory.setItem(i, coloredWool);
            }
        }
    }

    public void joinOrRespawn(NexiaPlayer player, boolean tp) {
        super.joinOrRespawn(player, tp);
        wasInSpawn.add(player.getUUID());
        player.reset(true, Minecraft.GameMode.SURVIVAL);
        player.unwrap().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000000, 1, true, true));
        setInventory(player);
    }

    private static ItemStack setWoolColor(ItemStack itemStack) {
        if (itemStack.getItem() != Items.WHITE_WOOL) return itemStack;

        if (woolId >= coloredWool.length) woolId = 0;
        itemStack = new ItemStack(coloredWool[woolId], itemStack.getCount());
        woolId++;
        return itemStack;
    }
    @Override
    public void fulfilKill(@NotNull NexiaPlayer player, @Nullable DamageSource source, @Nullable NexiaPlayer attacker) {

    }

    public void giveKillLoot(NexiaPlayer attacker, NexiaPlayer player) {
        if(!isFfaPlayer(attacker)) return;
        HashMap<Integer, ItemStack> availableRewards = (HashMap<Integer, ItemStack>) killRewards.clone();
        ArrayList<ItemStack> givenRewards = new ArrayList<>();


        for (int i = 0; i < Math.min(2, availableRewards.size()); i++) {
            // Pick reward
            int randomIndex = new Random().nextInt(availableRewards.size());
            int killRewardIndex = (Integer)availableRewards.keySet().toArray()[randomIndex];
            ItemStack reward = availableRewards.get(killRewardIndex);
            availableRewards.remove(killRewardIndex);

            // Give reward
            attacker.unwrap().inventory.add(reward.copy());
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

    public void killHeal(NexiaPlayer player) {
        if(!isFfaPlayer(player)) return;
        int minHeal = 4;
        int maxHeal = 11;
        float maxHealth = player.getMaxHealth();
        float lostHearts = maxHealth - player.getHealth();

        int heal = (int)(minHeal + (lostHearts - minHeal) * (maxHeal - minHeal) / (maxHealth - minHeal));

        if (player.unwrap().hasEffect(MobEffects.REGENERATION)) {
            player.unwrap().heal(heal);
        } else {
            player.unwrap().addEffect(new MobEffectInstance(MobEffects.REGENERATION, heal, 5, false, false));
        }
    }

    @Override
    public void finishSendToSpawn(NexiaPlayer player) {
        setInventory(player);
    }

    private static ItemStack gApplePotion() {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.setHoverName(ObjectMappings.convertComponent(MiniMessage.get().parse(String.format("<gradient:%s:%s>Golden Apple Juice</gradient>", "#ffaa00", "#ffc40e"))));
        potion.getOrCreateTag().putInt("CustomPotionColor", 16771584);

        ArrayList<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        effects.add(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        PotionUtils.setCustomEffects(potion, effects);

        return potion;
    }

    public boolean beforeBuild(NexiaPlayer player, BlockPos blockPos) {
        if (player.unwrap().isCreative()) return true;
        if (wasInSpawn.contains(player.getUUID()) || blockPos.getY() >= buildLimitY) {
            player.sendHandItemPacket();
            return false;
        }
        return canBuild(player, blockPos);
    }

    @Override
    public Minecraft.GameMode getMinecraftGameMode() {
        return Minecraft.GameMode.SURVIVAL;
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
        ItemStack snowball = new ItemStack(Items.SNOWBALL, 4);
        pearl.getOrCreateTag();
        cobweb.getOrCreateTag();
        snowball.getOrCreateTag();

        killRewards.put(3, gApplePotion());
        killRewards.put(4, pearl);
        killRewards.put(5, cobweb);
        killRewards.put(9, snowball);
    }
}
