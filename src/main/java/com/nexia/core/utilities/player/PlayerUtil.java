package com.nexia.core.utilities.player;

import com.google.gson.JsonParser;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.notcoded.codelib.minecraft.MinecraftAPI;
import net.notcoded.codelib.util.http.HttpAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerUtil {

    public static String getTextureID(@NotNull UUID uuid){
        String response;

        try {
            response = HttpAPI.get(new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", uuid)));
        } catch(Exception ignored) { return null; }

        if(response != null && !response.trim().isEmpty()) {
            String textureID = new JsonParser().parse(response).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            if(textureID == null || textureID.trim().isEmpty()) return null;
            return textureID;
        }

        return null;
    }



    public static ItemStack getPlayerHead(@NotNull UUID playerUUID) {
        ItemStack playerHead = Items.PLAYER_HEAD.getDefaultInstance();
        String textureID = getTextureID(playerUUID);

        if(textureID == null) return playerHead;
        playerHead.setTag(nbtFromTextureValue(playerUUID, textureID));

        return playerHead;
    }

    private static CompoundTag nbtFromTextureValue(@NotNull UUID id, @NotNull String textureID) {
        String name = MinecraftAPI.getName(id);

        CompoundTag nbtCompound = new CompoundTag();
        CompoundTag skullownertag = new CompoundTag();
        CompoundTag texturetag = new CompoundTag();
        ListTag texturelist = new ListTag();
        CompoundTag valuetag = new CompoundTag();
        CompoundTag displaytag = new CompoundTag();

        valuetag.putString("Value", textureID);
        texturelist.add(valuetag);
        texturetag.put("textures", texturelist);
        skullownertag.put("Properties", texturetag);
        skullownertag.putUUID("Id", id);
        nbtCompound.put("SkullOwner", skullownertag);
        displaytag.putString("Name", name);
        nbtCompound.put("display", displaytag);

        return nbtCompound;
    }

    public static void broadcastTitle(List<ServerPlayer> players, String title, String subtitle, int in, int stay, int out) {
        ClientboundSetTitlesPacket titlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title));
        ClientboundSetTitlesPacket subtitlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(subtitle));

        for (ServerPlayer player : players) {
            player.connection.send(new ClientboundSetTitlesPacket(in, stay, out));
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
    }

    public static boolean sendBossbar(CustomBossEvent customBossEvent, @Nullable Collection<ServerPlayer> collection) {
        if(collection == null) {
            customBossEvent.removeAllPlayers();
            return true;
        }
        return customBossEvent.setPlayers(collection);
    }

    public static boolean sendBossbar(CustomBossEvent customBossEvent, @NotNull Collection<NexiaPlayer> collection) {
        Collection<ServerPlayer> playerCollection = new ArrayList<>();
        collection.forEach(player -> playerCollection.add(player.unwrap()));
        return minecraftSendBossbar(customBossEvent, playerCollection);
    }

    public static boolean sendBossbar(CustomBossEvent customBossEvent, ServerPlayer player, boolean remove) {
        if(remove) {
            customBossEvent.removePlayer(player);
            return true;
        }
        if(customBossEvent.getPlayers().contains(player)) return false;
        customBossEvent.addPlayer(player);
        return true;
    }

    public static boolean sendBossbar(CustomBossEvent customBossEvent, NexiaPlayer player, boolean remove) {
        return sendBossbar(customBossEvent, player.unwrap(), remove);
    }

    public static void broadcastSound(List<ServerPlayer> players, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        for (ServerPlayer player : players) {
            sendSound(player, soundEvent, soundSource, volume, pitch);
        }
    }

    public static ServerPlayer getMinecraftPlayer(@NotNull UUID uuid){
        return ServerTime.minecraftServer.getPlayerList().getPlayer(uuid);
    }

    public static void sendTitle(ServerPlayer player, String title, String sub, int in, int stay, int out) {
        player.connection.send(new ClientboundSetTitlesPacket(in, stay, out));
        player.connection.send(new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title)));
        player.connection.send(new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(sub)));
    }

    public static ServerPlayer getPlayerAttacker(@NotNull ServerPlayer player, Entity attackerEntity) {

        if (player.getKillCredit() instanceof ServerPlayer) {
            return (ServerPlayer) player.getKillCredit();
        }

        if(player.getLastDamageSource() != null && player.getLastDamageSource().getEntity() != null) getPlayerAttacker(player.getLastDamageSource().getEntity());

        return null;
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

    public static boolean couldCrit(ServerPlayer player) {
        return !player.isOnGround() && player.fallDistance > 0.0f && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.onClimbable() && !player.isInWater() && !player.isPassenger();
    }

    public static void removeItem(ServerPlayer player, Item item, int count) {
        for (ItemStack itemStack : ItemStackUtil.getInvItems(player)) {
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

    public static void broadcastTitle(List<ServerPlayer> players, String title, String subtitle) {
        ClientboundSetTitlesPacket titlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.TITLE, new TextComponent(title));
        ClientboundSetTitlesPacket subtitlePacket = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.SUBTITLE, new TextComponent(subtitle));

        for (ServerPlayer player : players) {
            sendDefaultTitleLength(player);
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
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
