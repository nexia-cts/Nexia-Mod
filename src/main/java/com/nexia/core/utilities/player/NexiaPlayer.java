package com.nexia.core.utilities.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.notcoded.codelib.players.AccuratePlayer;

import java.util.Objects;

public record NexiaPlayer(AccuratePlayer player) {

    public void reset(boolean heal, GameType gameMode) {
        this.player.get().setInvulnerable(false);
        this.player.get().removeAllEffects();
        this.player.get().inventory.clearContent();
        this.player.get().clearFire();
        this.player.get().setExperienceLevels(0);
        this.player.get().setExperiencePoints(0);
        this.player.get().inventory.setCarried(ItemStack.EMPTY);
        this.player.get().getEnderChestInventory().clearContent();
        this.player.get().setGlowing(false);

        this.player.get().connection.send(new ClientboundStopSoundPacket());

        this.player.get().setGameMode(gameMode);

        if (heal) {
            this.player.get().setHealth(this.player.get().getMaxHealth());
            this.player.get().getFoodData().setFoodLevel(20);
        }


        this.player.get().abilities.mayfly = false;
        this.player.get().abilities.flying = false;
        Objects.requireNonNull(this.player.get().getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.0);
        this.player.get().onUpdateAbilities();

        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(this.player.get());
    }

    public void safeReset(boolean heal, GameType gameMode) {
        this.player.get().setInvulnerable(false);
        this.player.get().clearFire();
        this.player.get().removeAllEffects();
        this.player.get().setGlowing(false);
        this.player.get().setGameMode(gameMode);

        if (heal) {
            this.player.get().setHealth(this.player.get().getMaxHealth());
            this.player.get().getFoodData().setFoodLevel(20);
        }

        this.player.get().abilities.mayfly = false;
        this.player.get().abilities.flying = false;

        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(this.player.get());
    }

    public void leaveAllGames() {
        if (BwUtil.isInBedWars(this)) {
            BwPlayerEvents.leaveInBedWars(this);
        } else if (FfaUtil.isFfaPlayer(this)) {
            FfaUtil.leaveOrDie(this, this.player.get().getLastDamageSource(), true);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.LOBBY) {
            DuelGameHandler.leave(this, false);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.OITC) {
            OitcGame.leave(this);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.SKYWARS) {
            SkywarsGame.leave(this);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.FOOTBALL) {
            FootballGame.leave(this);
        }

        for (String tag : LobbyUtil.removedTags) {
            if (this.player.get().getTags().contains(tag)) this.player.get().removeTag(tag);
        }
    }

    public Player getFactoryPlayer() {
        return PlayerUtil.getFactoryPlayer(this.player.get());
    }

    public void sendMessage(Component component) {
        this.getFactoryPlayer().sendMessage(component);
    }

    public void sendActionBarMessage(Component component) {
        this.getFactoryPlayer().sendActionBarMessage(component);
    }

    public void sendTitle(Title title) {
        this.getFactoryPlayer().sendTitle(title);
    }

    public void refreshInventory() {
        NonNullList<ItemStack> i = NonNullList.create();
        for (int j = 0; j < this.player.get().containerMenu.slots.size(); ++j) {
            ItemStack itemStack = this.player.get().containerMenu.slots.get(j).getItem();
            i.add(itemStack.isEmpty() ? ItemStack.EMPTY : itemStack);
        }
        this.player.get().refreshContainer(this.player.get().containerMenu, i);
    }

    public void sendHandItemPacket(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            sendInvSlotPacket(this.player.get().inventory.selected);
        } else if (hand == InteractionHand.OFF_HAND) {
            sendInvSlotPacket(40);
        }
    }

    public void sendHandItemPacket() {
        this.sendHandItemPacket(this.player.get().getUsedItemHand());
    }

    public void sendInvSlotPacket(int slot) {
        int packetSlot;

        if (slot < 9) {
            packetSlot = 36 + slot;
        } else if (slot < 36) {
            packetSlot = slot;
        } else if (slot < 40) {
            packetSlot = 44 - slot;
        } else if (slot == 40) {
            packetSlot = 45;
        } else {
            return;
        }

        this.player.get().connection.send(new ClientboundContainerSetSlotPacket(0, packetSlot, this.player.get().inventory.getItem(slot)));
    }

    public void removeItem(Item item, int count) {
        for (ItemStack itemStack : ItemStackUtil.getInvItems(this.player.get())) {
            if (itemStack.getItem() != item) continue;

            if (itemStack.getCount() >= count) {
                itemStack.shrink(count);
                break;
            } else {
                count -= itemStack.getCount();
                itemStack.shrink(itemStack.getCount());
            }
        }
    }

    public void sendSound(SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        sendSound(new EntityPos(this.player.get()), soundEvent, soundSource, volume, pitch);
    }

    public void sendSound(EntityPos position, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        this.player.get().connection.send(new ClientboundSoundPacket(soundEvent, soundSource,
                position.x, position.y, position.z, 16f * volume, pitch));
    }
}
