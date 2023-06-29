package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.GameType;
import org.json.simple.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import static com.nexia.core.utilities.player.BanHandler.banTimeToText;
import static com.nexia.core.utilities.time.ServerTime.joinPlayer;
import static com.nexia.core.utilities.time.ServerTime.leavePlayer;

@Mixin(PlayerList.class)
public class PlayerListMixin {

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
                    args.set(0, joinFormat(component, joinPlayer));
                }
                if (
                        key.contains("multiplayer.player.left")
                ) {
                    args.set(0, leaveFormat(component, leavePlayer));
                }
            }

            if(!PlayerMutes.muted(player)){
                args.set(0, chatFormat(player, component));
            }

        } catch (Exception ignored) {}
    }

    @Inject(at = @At("RETURN"), method = "respawn")
    private void respawned(ServerPlayer oldPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = PlayerUtil.getFixedPlayer(oldPlayer);

        ServerLevel respawn = Main.server.getLevel(player.getRespawnDimension());

        if(LobbyUtil.isLobbyWorld(respawn)) {
            LobbyUtil.giveItems(player);
            player.setGameMode(GameType.ADVENTURE);
        }

        if (BwUtil.isInBedWars(player)) { BwPlayerEvents.respawned(player); }
    }

    private static Component joinFormat(Component original, ServerPlayer joinPlayer) {
        try {
            String name = String.valueOf(joinPlayer.getScoreboardName());
            if(ChatFormat.hasWhiteSpacesOrSpaces(null, name)) { return original; }
            if(joinPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) { return LegacyChatFormat.format("§8[§6!§8] §6{}", name); }
            return LegacyChatFormat.format("§8[§a+§8] §a{}", name);
        } catch (Exception var8) {
            return original;
        }
    }

    private static Component leaveFormat(Component original, ServerPlayer leavePlayer) {
        try {
            String name = String.valueOf(leavePlayer.getScoreboardName());
            if(ChatFormat.hasWhiteSpacesOrSpaces(null, name)) { return original; }
            return LegacyChatFormat.format("§8[§c-§8] §c{}", name);
        } catch (Exception var8) {
            return original;
        }
    }

    @Inject(method = "canPlayerLogin", at = @At("TAIL"), cancellable = true)
    private void checkIfBanned(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir){
        if (socketAddress == null || gameProfile == null) {
            return;
        }

        JSONObject object = BanHandler.getBanList(gameProfile.getId().toString());

        if(object != null) {
            long banTime = (long) object.get("duration") - System.currentTimeMillis();
            String reason = (String) object.get("reason");

            String textBanTime = banTimeToText(banTime);

            if(banTime > 0){
                cir.setReturnValue(new TextComponent("§c§lYou have been banned.\n§7Duration: §d" + textBanTime + "\n§7Reason: §d" + reason + "\n§7You can appeal your ban at §d" + com.nexia.discord.Main.config.discordLink));
            } else {
                BanHandler.removeBanFromList(gameProfile);
            }
        }
    }

    @Inject(method = "canBypassPlayerLimit", at = @At("TAIL"), cancellable = true)
    private void playerLimitBypasser(GameProfile gameProfile, CallbackInfoReturnable<Boolean> cir) {
        CompletableFuture<Boolean> bool = Permissions.check(gameProfile, "nexia.join.full");
        try {
            boolean value = bool.get();
            cir.setReturnValue(value);
        } catch(Exception ignored) { }
    }

    @Inject(method = "placeNewPlayer", at = @At("INVOKE"))
    private void setJoinMessage(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci){
        joinPlayer = serverPlayer;
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
