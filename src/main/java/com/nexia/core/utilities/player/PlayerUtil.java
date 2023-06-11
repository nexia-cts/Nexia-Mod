package com.nexia.core.utilities.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class PlayerUtil {
    private static void sendDefaultTitleLength(ServerPlayer player) {
        player.connection.send(new ClientboundSetTitlesPacket(10, 60, 20));
    }

    public static Player getFactoryPlayer(@NotNull net.minecraft.world.entity.player.Player minecraftPlayer) {
        return getFactoryPlayerFromName(minecraftPlayer.getScoreboardName());
    }

    public static Player getFactoryPlayerFromName(@NotNull String player) {
        if(player.trim().length() == 0) return null;
        return ServerTime.factoryServer.getPlayer(player);
    }

    public static ServerPlayer getMinecraftPlayer(@NotNull Player player){
        return PlayerUtil.getMinecraftPlayerFromName(player.getRawName());
    }

    public static ServerPlayer getMinecraftPlayerFromName(@NotNull String player){
        return ServerTime.minecraftServer.getPlayerList().getPlayerByName(player);
    }

    public static void resetHealthStatus(@NotNull Player player) {
        player.setInvulnerabilityTime(0);
        player.clearEffects();
        player.setHealth(20);
        player.setFoodLevel(20);
    }
    public static boolean hasPermission(@NotNull CommandSourceStack permission, @NotNull String command, int level) {
        return me.lucko.fabric.api.permissions.v0.Permissions.check(permission, command, level);
    }

    public static ServerPlayer getPlayerAttacker(Entity attackerEntity) {
        if (attackerEntity == null) return null;

        if (attackerEntity instanceof ServerPlayer) {
            return (ServerPlayer) attackerEntity;

        } else if (attackerEntity instanceof Projectile) {
            Entity projectileOwner = ((Projectile) attackerEntity).getOwner();
            if (!(projectileOwner instanceof ServerPlayer)) return null;
            return (ServerPlayer) projectileOwner;
        }
        return null;
    }

    public static void sendSound(ServerPlayer player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        sendSound(player, new EntityPos(player), soundEvent, soundSource, volume, pitch);
    }

    public static ServerPlayer getFixedPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer == null) return null;
        return ServerTime.minecraftServer.getPlayerList().getPlayer(serverPlayer.getUUID());
    }

    public static void sendSound(ServerPlayer player, EntityPos position, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(soundEvent, soundSource,
                position.x, position.y, position.z, 16f * volume, pitch));
    }

}
