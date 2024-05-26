package com.nexia.minigames.games.bedwars.upgrades;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Objects;

public class BwApplyUpgrades {

    public static void upgradeSecond() {
        for (BwTeam team : BwTeam.allTeams.values()) {
            if (team == null || team.players == null) continue;

            applySharpness(team);
            applyProtection(team);
            applyHaste(team);
            applyHealing(team);
        }
    }

    private static void applySharpness(BwTeam team) {
        int level = team.upgrades.get(BwUpgrade.UPGRADE_KEY_SHARPNESS).level;

        for (NexiaPlayer player : team.players) {
            for (ItemStack itemStack : player.unwrap().inventory.items) {
                if (itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof TridentItem) {
                    ItemStackUtil.enchant(itemStack, Enchantments.SHARPNESS, level);
                }
            }
        }
    }

    private static void applyProtection(BwTeam team) {
        int level = team.upgrades.get(BwUpgrade.UPGRADE_KEY_PROTECTION).level;
        if (level < 1) return;

        for (NexiaPlayer player : team.players) {
            NonNullList<ItemStack> armor = player.unwrap().inventory.armor;
            ItemStackUtil.enchant(armor.get(0), Enchantments.ALL_DAMAGE_PROTECTION, level);
            ItemStackUtil.enchant(armor.get(1), Enchantments.ALL_DAMAGE_PROTECTION, level);
            ItemStackUtil.enchant(armor.get(2), Enchantments.ALL_DAMAGE_PROTECTION, level);
            ItemStackUtil.enchant(armor.get(3), Enchantments.ALL_DAMAGE_PROTECTION, level);
        }
    }

    private static void applyHaste(BwTeam team) {
        int level = team.upgrades.get(BwUpgrade.UPGRADE_KEY_HASTE).level;
        if (level < 1) return;

        int effectLevel = level - 1;
        for (NexiaPlayer player : team.players) {
            if (!player.unwrap().hasEffect(MobEffects.DIG_SPEED) || Objects.requireNonNull(player.unwrap().getEffect(MobEffects.DIG_SPEED)).getAmplifier() != effectLevel) {
                player.unwrap().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1000000, effectLevel, false, false));
                BwUtil.setAttackSpeed(player.unwrap());
            }
        }
    }

    private static void applyHealing(BwTeam team) {
        int level = team.upgrades.get(BwUpgrade.UPGRADE_KEY_HEALING).level;
        if (level < 1) return;

        for (NexiaPlayer player : team.players) {
            if (team.spawn.isInRadius(new EntityPos(player.unwrap()), 24)) {
                if (!player.unwrap().hasEffect(MobEffects.REGENERATION)) {
                    player.unwrap().addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, level - 1, false, false));
                }

            } else if (player.unwrap().hasEffect(MobEffects.REGENERATION) &&
                    Objects.requireNonNull(player.unwrap().getEffect(MobEffects.REGENERATION)).getAmplifier() == 0) {
                player.unwrap().removeEffect(MobEffects.REGENERATION);
            }
        }
    }

}
