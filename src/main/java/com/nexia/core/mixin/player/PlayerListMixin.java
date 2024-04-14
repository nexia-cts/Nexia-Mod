package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ComponentArgument;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.net.SocketAddress;
import java.time.LocalDateTime;

import static com.nexia.core.utilities.player.BanHandler.banTimeToText;
import static com.nexia.core.utilities.player.BanHandler.getBanTime;
import static com.nexia.core.utilities.time.ServerTime.leavePlayer;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Unique
    private ServerPlayer joinPlayer = null;

    @ModifyArgs(method = "broadcastMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundChatPacket;<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void handleChat(Args args) {
        try {
            Component component = args.get(0);
            ServerPlayer player = ServerTime.minecraftServer.getPlayerList().getPlayer(args.get(2));
            String key = ((TranslatableComponent) component).getKey();

            if (key.contains("multiplayer.player.left")) args.set(0, leaveFormat(component, leavePlayer));
            if (key.contains("multiplayer.player.join")) args.set(0, joinFormat(component, joinPlayer));

            if(!PlayerMutes.muted(player)){
                args.set(0, chatFormat(component));
            }

        } catch (Exception ignored) {}
    }

    @Inject(at = @At("RETURN"), method = "respawn")
    private void respawned(ServerPlayer oldPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = PlayerUtil.getFixedPlayer(oldPlayer);

        ServerLevel respawn = ServerTime.minecraftServer.getLevel(player.getRespawnDimension());

        if(FfaSkyUtil.isFfaPlayer(player)) {
            FfaSkyUtil.joinOrRespawn(player);
            return;
        }

        if(respawn != null && LobbyUtil.isLobbyWorld(respawn)) {
            player.inventory.clearContent();
            LobbyUtil.giveItems(player);
            player.setGameMode(GameType.ADVENTURE);

            PlayerUtil.getFactoryPlayer(player).runCommand("/hub");
            return;
        }

        if (BwUtil.isInBedWars(player)) { BwPlayerEvents.respawned(player); }
    }

    @Unique
    private static Component joinFormat(Component original, ServerPlayer joinPlayer) {
        try {
            String name = joinPlayer.getScoreboardName();
            if(name.isEmpty()) { return original; }

            Component component;
            boolean firstJoiner = joinPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1;

            try {
                // this is so hacky lmfao

                component = ComponentArgument.textComponent().parse(new StringReader(
                        "[{\"text\":\"[\",\"color\":\"#4A4A4A\"},{\"text\":\"+\",\"color\":\"" + ChatFormat.greenColor.asHexString().toUpperCase() + "\"},{\"text\":\"]\",\"color\":\"#4A4A4A\"},{\"text\":\" " + name + "\",\"color\":\"" + ChatFormat.greenColor.asHexString().toUpperCase() + "\"}]"
                ));

                if(firstJoiner) {
                    component = ComponentArgument.textComponent().parse(new StringReader(
                            "[{\"text\":\"[\",\"color\":\"#4A4A4A\"},{\"text\":\"!\",\"color\":\"" + ChatFormat.goldColor.asHexString().toUpperCase() + "\"},{\"text\":\"]\",\"color\":\"#4A4A4A\"},{\"text\":\" " + name + "\",\"color\":\"" + ChatFormat.goldColor.asHexString().toUpperCase() + "\"}]"
                    ));
                }

            } catch (Throwable ignored) {
                component = LegacyChatFormat.format("§8[§a+§8] §a{}", name);

                if(firstJoiner) {
                    component = LegacyChatFormat.format("§8[§6!§8] §6{}", name);
                }
            }


            return component;
        } catch (Exception var8) {
            return original;
        }
    }

    @Unique
    private static Component leaveFormat(Component original, ServerPlayer leavePlayer) {
        try {
            String name = leavePlayer.getScoreboardName();
            if(name.isEmpty()) { return original; }

            Component component;
            try {
                // this is so hacky lmfao
                component = ComponentArgument.textComponent().parse(new StringReader(
                        "[{\"text\":\"[\",\"color\":\"#4A4A4A\"},{\"text\":\"-\",\"color\":\"" + ChatFormat.failColor.asHexString().toUpperCase() + "\"},{\"text\":\"]\",\"color\":\"#4A4A4A\"},{\"text\":\" " + name + "\",\"color\":\"" + ChatFormat.failColor.asHexString().toUpperCase() + "\"}]"
                ));
            } catch (Throwable ignored) {
                component = LegacyChatFormat.format("§8[§c-§8] §c{}", name);
            }

            return component;
        } catch (Exception var8) {
            return original;
        }
    }

    @Inject(method = "canPlayerLogin", at = @At("TAIL"), cancellable = true)
    private void checkIfBanned(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir){
        if (socketAddress == null || gameProfile == null) {
            return;
        }

        JSONObject banJSON = BanHandler.getBanList(gameProfile.getId().toString());

        if(banJSON != null) {
            LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
            String reason = (String) banJSON.get("reason");

            String textBanTime = banTimeToText(banTime);

            if(LocalDateTime.now().isBefore(banTime)){
                cir.setReturnValue(new TextComponent("§c§lYou have been banned.\n§7Duration: §d" + textBanTime + "\n§7Reason: §d" + reason + "\n§7You can appeal your ban at §d" + com.nexia.discord.Main.config.discordLink));
            } else {
                BanHandler.removeBanFromList(gameProfile);
            }
        }
    }

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void setJoinMessage(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci){
        joinPlayer = serverPlayer;
    }

    @Unique
    private static Component chatFormat(Component original) {
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
