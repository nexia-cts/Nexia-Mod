package com.nexia.ffa;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FfaUtil {

    public static final String FFA_TAG = "ffa";

    public static boolean isFfaPlayer(NexiaPlayer player) {
        return (player.hasTag(FFA_TAG)
                || player.hasTag("ffa_classic")
                || player.hasTag("ffa_kits")
                || player.hasTag("ffa_sky")
                || player.hasTag("ffa_uhc"))
                && ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode == PlayerGameMode.FFA;
    }

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

    public static void leaveOrDie(@NotNull NexiaPlayer player, @Nullable DamageSource source, boolean leaving) {
        player.unwrap().inventory.setCarried(ItemStack.EMPTY);
        CorePlayerData data = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);

        if(data.ffaGameMode == FfaGameMode.CLASSIC) {
            FfaClassicUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(data.ffaGameMode == FfaGameMode.KITS) {
            FfaKitsUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(data.ffaGameMode == FfaGameMode.SKY) {
            FfaSkyUtil.wasInSpawn.remove(player.getUUID());
            FfaSkyUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(data.ffaGameMode == FfaGameMode.UHC) {
            FfaUhcUtil.leaveOrDie(player, source, leaving);
            return;
        }

        if(leaving) data.ffaGameMode = null;
    }

    public static boolean isFfaWorld(Level level) {
        return level == com.nexia.ffa.classic.utilities.FfaAreas.ffaWorld ||
                level == com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld ||
                level == com.nexia.ffa.sky.utilities.FfaAreas.ffaWorld ||
                level == com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld;
    }

    public static Component returnDeathMessage(@NotNull NexiaPlayer player, @Nullable DamageSource source) {

        String name = player.getRawName();

        Component invalid = Component.text("Wow,").color(ChatFormat.chatColor2)
                .append(Component.text(" ☠ " + name).color(ChatFormat.failColor))
                .append(Component.text(" somehow killed themselves.").color(ChatFormat.chatColor2));

        if(source == null) return invalid;

        if (source == DamageSource.OUT_OF_WORLD) {
            return Component.text("⚐ " + name).color(ChatFormat.failColor)
                    .append(Component.text(" took a ride to the void.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.LAVA) {
            return Component.text("\uD83D\uDD25 " + name).color(ChatFormat.failColor)
                    .append(Component.text(" was deepfried in lava.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.HOT_FLOOR) {
            return Component.text("\uD83D\uDD25 " + name).color(ChatFormat.failColor)
                    .append(Component.text(" stepped on hot legos.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
            return Component.text("\uD83D\uDD25 " + name).color(ChatFormat.failColor)
                    .append(Component.text(" comBusted.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.FALL) {
            return Component.text("⚓ " + name).color(ChatFormat.failColor)
                    .append(Component.text(" turned into a human doormat.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.CACTUS) {
            return Component.text("ʕっ·ᴥ·ʔっ ").color(ChatFormat.chatColor2)
                    .append(Component.text("☠ " + name).color(ChatFormat.failColor))
                    .append(Component.text(" hugged a cactus.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.DROWN || source == DamageSource.DRY_OUT) {
            return Component.text("\uD83C\uDF0A " + name).color(ChatFormat.failColor)
                    .append(Component.text(" had a bit too much to drink.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.MAGIC) {
            return Component.text("\uD83E\uDDEA " + name).color(ChatFormat.failColor)
                    .append(Component.text(" had a bit too much pot.").color(ChatFormat.chatColor2));
        }

        return invalid;
    }

    public static Component returnClassicDeathMessage(@NotNull NexiaPlayer player, @NotNull NexiaPlayer attacker) {
        if (attacker.equals(player)) return null;

        String symbol = "◆";
        Item handItem = attacker.unwrap().getMainHandItem().getItem();
        String itemName = new ItemStack(handItem).getDisplayName().toString().toLowerCase();

        if (itemName.contains("sword")) {
            symbol = "\uD83D\uDDE1";
        } else if (handItem == Items.TRIDENT) {
            symbol = "\uD83D\uDD31";
        } else if (itemName.contains("axe")) {
            symbol = "\uD83E\uDE93";
        } else if (handItem == Items.BOW || handItem == Items.CROSSBOW) {
            symbol = "\uD83C\uDFF9";
        } else if (itemName.contains("hoe")) {
            symbol = "Γ";
        }


        return Component.text("☠ " + player.getRawName()).color(ChatFormat.failColor)
                .append(Component.text(" was killed by ").color(ChatFormat.chatColor2))
                .append(Component.text(symbol + " " + attacker.getRawName()).color(ChatFormat.greenColor))
                .append(Component.text(" with ").color(ChatFormat.chatColor2))
                .append(Component.text(FfaUtil.calculateHealth(attacker.getHealth()) + "❤").color(ChatFormat.failColor))
                .append(Component.text(" left.").color(ChatFormat.chatColor2));
    }
}
