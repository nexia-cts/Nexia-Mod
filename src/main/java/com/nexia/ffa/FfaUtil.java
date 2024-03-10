package com.nexia.ffa;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FfaUtil {

    public static final String FFA_TAG = "ffa";

    public static boolean isFfaPlayer(net.minecraft.world.entity.player.Player player) {
        return player.getTags().contains("ffa") || player.getTags().contains("ffa_classic") || player.getTags().contains("ffa_kits") || player.getTags().contains("ffa_sky") || player.getTags().contains("ffa_uhc") && com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.FFA;
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

    public static void leaveOrDie(@NotNull ServerPlayer player, @Nullable DamageSource source, boolean leaving) {
        player.inventory.setCarried(ItemStack.EMPTY);
        PlayerData data = PlayerDataManager.get(player);

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

    public static Component returnDeathMessage(@NotNull ServerPlayer minecraftPlayer, @Nullable DamageSource source) {

        Component invalid = Component.text("Wow,").color(ChatFormat.chatColor2)
                .append(Component.text(" ☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor))
                .append(Component.text(" somehow killed themselves.").color(ChatFormat.chatColor2));

        if(source == null) return invalid;

        if (source == DamageSource.OUT_OF_WORLD) {
            return Component.text("⚐ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" took a ride to the void.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.LAVA) {
            return Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" was deepfried in lava.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.HOT_FLOOR) {
            return Component.text("⚐ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" took a ride to the void.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.LAVA) {
            return Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" was deepfried in lava.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.HOT_FLOOR) {
            return Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" stepped on hot legos.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
            return Component.text("\uD83D\uDD25 " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" comBusted.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.FALL) {
            return Component.text("⚓ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" turned into a human doormat.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.CACTUS) {
            return Component.text("ʕっ·ᴥ·ʔっ ").color(ChatFormat.chatColor2)
                    .append(Component.text("☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor))
                    .append(Component.text(" hugged a cactus.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.DROWN || source == DamageSource.DRY_OUT) {
            return Component.text("\uD83C\uDF0A " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" had a bit too much to drink.").color(ChatFormat.chatColor2));
        } else if (source == DamageSource.MAGIC) {
            return Component.text("\uD83E\uDDEA " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                    .append(Component.text(" had a bit too much pot.").color(ChatFormat.chatColor2));
        }

        return invalid;
    }

    public static Component returnClassicDeathMessage(@NotNull ServerPlayer minecraftPlayer, @NotNull ServerPlayer attacker) {
        if (attacker == minecraftPlayer) return null;

        String symbol = "◆";
        Item handItem = attacker.getMainHandItem().getItem();
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

        return Component.text("☠ " + minecraftPlayer.getScoreboardName()).color(ChatFormat.failColor)
                .append(Component.text(" was killed by ").color(ChatFormat.chatColor2))
                .append(Component.text(symbol + " " + attacker.getScoreboardName()).color(ChatFormat.greenColor))
                .append(Component.text(" with ").color(ChatFormat.chatColor2))
                .append(Component.text(FfaUtil.calculateHealth(attacker.getHealth()) + "❤").color(ChatFormat.failColor))
                .append(Component.text(" left.").color(ChatFormat.chatColor2));
    }
}
