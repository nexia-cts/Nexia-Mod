package com.nexia.core.mixin.player;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.player.SavedPlayerData;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    // Create player data on join
    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    private void placeNewPlayerHead(Connection connection, ServerPlayer player, CallbackInfo ci) {
        PlayerDataManager.addPlayerData(player);
        com.nexia.ffa.utilities.player.PlayerDataManager.addPlayerData(player);
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.addPlayerData(player);
        if(player.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) <= 1) {
            if(Main.config.events.serverFirstJoinCommands.length >= 2 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverFirstJoinCommands, null)){
                for(int i = 0; i < Main.config.events.serverFirstJoinCommands.length; i++){
                    PlayerUtil.executeServerCommand(Main.config.events.serverFirstJoinCommands[i], player);
                }
            } else if(Main.config.events.serverFirstJoinCommands.length <= 1 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverFirstJoinCommands, null)){
                PlayerUtil.executeServerCommand(Main.config.events.serverFirstJoinCommands[0], player);
            }
            if(Main.config.events.playerFirstJoinCommands.length >= 2 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerFirstJoinCommands, null)){
                for(int i = 0; i < Main.config.events.playerFirstJoinCommands.length; i++){
                    PlayerUtil.executePlayerCommand(player, Main.config.events.playerFirstJoinCommands[i]);
                }
            } else if(Main.config.events.playerFirstJoinCommands.length <= 1 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerFirstJoinCommands, null)){
                PlayerUtil.executePlayerCommand(player, Main.config.events.playerFirstJoinCommands[0]);
            }
        }
        if(Main.config.events.serverJoinCommands.length >= 2 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverJoinCommands, null)){
            for(int i = 0; i < Main.config.events.serverJoinCommands.length; i++){
                PlayerUtil.executeServerCommand(Main.config.events.serverJoinCommands[i], player);
            }
        } else if(Main.config.events.serverJoinCommands.length <= 1 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverJoinCommands, null)){
            PlayerUtil.executeServerCommand(Main.config.events.serverJoinCommands[0], player);
        }
        if(Main.config.events.playerJoinCommands.length >= 2 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverJoinCommands, null)){
            for(int i = 0; i < Main.config.events.playerJoinCommands.length; i++){
                PlayerUtil.executePlayerCommand(player, Main.config.events.playerJoinCommands[i]);
            }
        } else if(Main.config.events.playerJoinCommands.length <= 1 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerJoinCommands, null)){
            PlayerUtil.executePlayerCommand(player, Main.config.events.playerJoinCommands[0]);
        }
    }

    // Create player data on join
    @Inject(at = @At("TAIL"), method = "placeNewPlayer")
    private void placeNewPlayerTail(Connection connection, ServerPlayer player, CallbackInfo ci) {
        LobbyUtil.leaveAllGames(player, true);
        player.sendMessage(new TextComponent(ChatFormat.separatorLine("Welcome")), Util.NIL_UUID);
        player.sendMessage(ChatFormat.format(" §8» {b1}Welcome {b2}{} {b1}to {b2}Nexia{b1}!", player.getScoreboardName()), Util.NIL_UUID);
        player.sendMessage(ChatFormat.format(" §8» {b1}Players online: {b2}{}", Main.server.getPlayerCount()), Util.NIL_UUID);
        player.sendMessage(ChatFormat.format(" §8» {b1}Read the rules: {b2}/rules"), Util.NIL_UUID);
        player.sendMessage(ChatFormat.format(" §8» {b1}Join our discord: ").append(new TextComponent(ChatFormat.brandColor2 + "\247n" + Main.config.discordLink).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.config.discordLink)))), Util.NIL_UUID);
        player.sendMessage(new TextComponent(ChatFormat.separatorLine(null)), Util.NIL_UUID);
    }

    // Force-leave on server leave

    @Inject(at = @At("HEAD"), method = "remove")
    private void removeHead(ServerPlayer player, CallbackInfo ci) {
        LobbyUtil.leaveAllGames(player, false);
        if(Main.config.events.serverLeaveCommands.length >= 2 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverLeaveCommands, null)){
            for(int i = 0; i < Main.config.events.serverLeaveCommands.length; i++){
                PlayerUtil.executeServerCommand(Main.config.events.serverLeaveCommands[i], player);
            }
        } else if(Main.config.events.serverFirstJoinCommands.length <= 1 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.serverFirstJoinCommands, null)){
            PlayerUtil.executeServerCommand(Main.config.events.serverLeaveCommands[0], player);
        }
        if(Main.config.events.playerLeaveCommands.length >= 2 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerLeaveCommands, null)){
            for(int i = 0; i < Main.config.events.playerLeaveCommands.length; i++){
                PlayerUtil.executePlayerCommand(player, Main.config.events.playerLeaveCommands[i]);
            }
        } else if(Main.config.events.playerLeaveCommands.length <= 1 && !ChatFormat.hasWhiteSpacesOrSpaces(Main.config.events.playerLeaveCommands, null)){
            PlayerUtil.executePlayerCommand(player, Main.config.events.playerLeaveCommands[0]);
        }
    }

    // Clear player data on leave
    @Inject(at = @At("TAIL"), method = "remove")
    private void removeTail(ServerPlayer player, CallbackInfo ci) {
        PlayerDataManager.removePlayerData(player);
        com.nexia.ffa.utilities.player.PlayerDataManager.removePlayerData(player);
        com.nexia.minigames.games.duels.util.player.PlayerDataManager.removePlayerData(player);
    }

    @Inject(at = @At("RETURN"), method = "respawn")
    private void respawned(ServerPlayer oldPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = PlayerUtil.getFixedPlayer(oldPlayer);

        if (BwUtil.isInBedWars(player)) {
            BwPlayerEvents.respawned(player);
        }
    }

    @ModifyArgs(method = "broadcastMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundChatPacket;<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void handleChat(Args args) {
        try {
            Component component = args.get(0);
            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(args.get(2));

            if(Main.config.events.statusMessages){
                String key = ((TranslatableComponent) component).getKey();
                if (
                        key.contains("multiplayer.player.join")
                ) {
                    args.set(0, joinFormat(component, player));
                }
                if (
                        key.contains("multiplayer.player.left")
                ) {
                    args.set(0, leaveFormat(component, player));
                }
            }

            if(!PlayerMutes.muted(player)){
                args.set(0, chatFormat(player, component));
            }

        } catch (Exception ignored) {}
    }

    private static Component joinFormat(Component original, ServerPlayer joinPlayer) {
        try {
            String name = String.valueOf(joinPlayer.getScoreboardName());
            if(ChatFormat.hasWhiteSpacesOrSpaces(null, name)) { return original; }
            if(joinPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) { return ChatFormat.format(Main.config.events.firstJoinMessage.replaceAll("%player%", name)); }
            return ChatFormat.format(Main.config.events.joinMessage.replaceAll("%player%", name));
        } catch (Exception var8) {
            return original;
        }
    }

    private static Component leaveFormat(Component original, ServerPlayer leavePlayer) {
        try {
            String name = String.valueOf(leavePlayer.getScoreboardName());
            if(ChatFormat.hasWhiteSpacesOrSpaces(null, name)) { return original; }
            return ChatFormat.format(Main.config.events.leaveMessage.replaceAll("%player%", name));
        } catch (Exception var8) {
            return original;
        }
    }

    private static Component chatFormat(ServerPlayer player, Component original) {
        try {
            TranslatableComponent component = (TranslatableComponent) original;
            Object[] args = component.getArgs();

            MutableComponent name = (MutableComponent) args[0];
            MutableComponent suffix = new TextComponent(" » ").withStyle(ChatFormatting.GRAY);

            String messageString = (String) args[1];
            MutableComponent message = new TextComponent(messageString).withStyle(ChatFormatting.WHITE);

            return name.append(suffix).append(message);

        } catch (Exception e) {
            return original;
        }
    }

}
