package com.nexia.core.utilities.player;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.nexus.api.world.effect.StatusEffectInstance;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.api.world.item.ItemStack;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import com.nexia.nexus.builder.implementation.world.entity.player.WrappedPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NexiaPlayer extends WrappedPlayer {

    public NexiaPlayer(ServerPlayer wrappedPlayer) {
        super(wrappedPlayer);
    }

    public NexiaPlayer(Player player) {
        super(PlayerUtil.getMinecraftPlayer(player.getUUID()));
    }

    public void reset(boolean heal, Minecraft.GameMode gameMode) {
        this.setInvulnerabilityTime(0);
        this.clearEffects();
        this.getInventory().clear();
        this.setRemainingFireTicks(0);
        this.setExperienceLevel(0);
        this.setExperiencePoints(0);
        this.getInventory().setCursorStack(ItemStack.create(Minecraft.Item.AIR));

        this.unwrap().getEnderChestInventory().clearContent();
        this.unwrap().setGlowing(false);
        this.unwrap().connection.send(new ClientboundStopSoundPacket());

        this.unwrap().setGameMode(ObjectMappings.GAME_MODES.get(gameMode));

        if (heal) {
            this.setHealth(this.unwrap().getMaxHealth());
            this.setFoodLevel(20);
        }

        this.setAbleToFly(false);

        Objects.requireNonNull(this.unwrap().getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.0);
        this.unwrap().onUpdateAbilities();

        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(this.unwrap());
    }

    public boolean isInGameMode(PlayerGameMode gameMode) {
        return PlayerDataManager.get(this).gameMode.equals(gameMode);
    }

    public void setGameMode(Minecraft.GameMode gameMode) {
        this.unwrap().setGameMode(ObjectMappings.GAME_MODES.get(gameMode));
    }

    public void safeReset(boolean heal, Minecraft.GameMode gameMode) {
        this.setInvulnerabilityTime(0);
        this.clearEffects();
        this.setRemainingFireTicks(0);
        this.unwrap().setGlowing(false);

        this.setGameMode(gameMode);

        if (heal) {
            this.setHealth(this.unwrap().getMaxHealth());
            this.setFoodLevel(20);
        }

        this.setAbleToFly(false);
        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(this.unwrap());
    }

    public boolean hasPermission(@NotNull String permission) {
        return Permissions.check(this.unwrap(), permission);
    }


    public boolean hasPermission(@NotNull String permission, int defaultRequiredLevel) {
        return Permissions.check(this.unwrap(), permission, defaultRequiredLevel);
    }

    public boolean hasEffect(Minecraft.Effect effect) {
        for(StatusEffectInstance effectInstance : this.getActiveEffects()) {
            if(effectInstance.getStatusEffect().getType().equals(effect.getType())) {
                return true;
            }
        }
        return false;
    }

    public void leaveAllGames() {
        if (BwUtil.isInBedWars(this)) {
            BwPlayerEvents.leaveInBedWars(this);
        } else if (FfaUtil.isFfaPlayer(this)) {
            FfaUtil.leaveOrDie(this, this.unwrap().getLastDamageSource(), true);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.LOBBY) {
            DuelGameHandler.leave(this, false);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.OITC) {
            OitcGame.leave(this);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.SKYWARS) {
            SkywarsGame.leave(this);
        } else if (PlayerDataManager.get(this).gameMode == PlayerGameMode.FOOTBALL) {
            FootballGame.leave(this);
        }

        // sometimes those do not get removed
        PlayerUtil.sendBossbar(SkywarsGame.BOSSBAR, this, true);
        BwScoreboard.removeScoreboardFor(this);

        for (String tag : LobbyUtil.removedTags) {
            if (this.hasTag(tag)) this.removeTag(tag);
        }
    }

    public void refreshInventory() {
        NonNullList<net.minecraft.world.item.ItemStack> i = NonNullList.create();
        for (int j = 0; j < this.unwrap().containerMenu.slots.size(); ++j) {
            net.minecraft.world.item.ItemStack itemStack = this.unwrap().containerMenu.slots.get(j).getItem();
            i.add(itemStack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : itemStack);
        }
        this.unwrap().refreshContainer(this.unwrap().containerMenu, i);
    }

    public void sendHandItemPacket(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            sendInvSlotPacket(this.unwrap().inventory.selected);
        } else if (hand == InteractionHand.OFF_HAND) {
            sendInvSlotPacket(40);
        }
    }

    public void sendHandItemPacket() {
        this.sendHandItemPacket(this.unwrap().getUsedItemHand());
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

        this.unwrap().connection.send(new ClientboundContainerSetSlotPacket(0, packetSlot, this.unwrap().inventory.getItem(slot)));
    }

    public void removeItem(Item item, int count) {
        for (net.minecraft.world.item.ItemStack itemStack : ItemStackUtil.getInvItems(this.unwrap())) {
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
        sendSound(new EntityPos(this), soundEvent, soundSource, volume, pitch);
    }

    public void sendSound(EntityPos position, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        this.unwrap().connection.send(new ClientboundSoundPacket(soundEvent, soundSource,
                position.x, position.y, position.z, 16f * volume, pitch));
    }
}
