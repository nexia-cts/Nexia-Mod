package com.nexia.core.listeners.nexus;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.nexus.api.event.player.PlayerPlaceBlockEvent;
import com.nexia.nexus.api.event.player.PlayerUseItemEvent;
import com.nexia.nexus.api.world.item.ItemStack;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.builder.implementation.world.effect.WrappedStatusEffectInstance;
import com.nexia.nexus.builder.implementation.world.item.WrappedItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Random;

public class PlayerUseItemListener {
    private void eatGoldenHead(NexiaPlayer player, ItemStack itemStack) {
        player.addEffectInstance(new WrappedStatusEffectInstance(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100)));
        player.addEffectInstance(new WrappedStatusEffectInstance(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 1)));
        player.addEffectInstance(new WrappedStatusEffectInstance(new MobEffectInstance(MobEffects.REGENERATION, 100, 2)));
        player.unwrap().playNotifySound(SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, new Random().nextFloat() * 0.1F + 0.9F);

        player.unwrap().getCooldowns().addCooldown(((WrappedItemStack) itemStack).unwrap().getItem(), 240);
        if (!player.unwrap().isCreative()) itemStack.decrementCount();
        player.refreshInventory();
    }

    public void registerListener() {
        PlayerUseItemEvent.BACKEND.register(playerUseItemEvent -> {

            NexiaPlayer player = new NexiaPlayer(playerUseItemEvent.getPlayer());

            ItemStack itemStack = playerUseItemEvent.getItemStack();

            String name = itemStack.getDisplayName().toString().toLowerCase();

            PlayerGameMode gameMode = ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode;

            if(gameMode == PlayerGameMode.LOBBY) {
                if (name.contains("gamemode selector")) {
                    //PlayGUI.openMainGUI(minecraftPlayer);
                    player.runCommand("/play", 0, false);
                    return;
                }

                if (name.contains("prefix selector")) {
                    //PrefixGUI.openRankGUI(minecraftPlayer);
                    player.runCommand("/prefix", 0, false);
                    return;
                }

                if (name.contains("duel sword") && !name.contains("custom duel sword")) {
                    //QueueGUI.openQueueGUI(minecraftPlayer);
                    player.runCommand("/queue", 0, false);
                    return;
                }

                if (name.contains("team axe")) {
                    player.runCommand("/party list");
                }
            }

            if (name.contains("golden head") && itemStack.getItem() == Minecraft.Item.PLAYER_HEAD && !FfaUhcUtil.INSTANCE.isInFfaSpawn(player)) {
                eatGoldenHead(player, itemStack);
                playerUseItemEvent.setCancelled(true);

            }
        });

        PlayerPlaceBlockEvent.BACKEND.register(playerPlaceBlockEvent -> {
            NexiaPlayer player = new NexiaPlayer(playerPlaceBlockEvent.getPlayer());

            ItemStack itemStack = playerPlaceBlockEvent.getBlockStack();
            if (itemStack.getDisplayName().toString().toLowerCase().contains("golden head") && itemStack.getItem() == Minecraft.Item.PLAYER_HEAD && !FfaUhcUtil.INSTANCE.isInFfaSpawn(player)) {
                eatGoldenHead(player, itemStack);
                playerPlaceBlockEvent.setCancelled(true);
            }
        });
    }
}
