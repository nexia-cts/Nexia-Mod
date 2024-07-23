package com.nexia.core.mixin.player;

import com.mojang.authlib.GameProfile;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.discord.NexiaDiscord;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import de.themoep.minedown.adventure.MineDown;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
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
import java.util.UUID;

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

            assert player != null;
            NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
            if(!PlayerMutes.muted(nexiaPlayer)){
                args.set(0, chatFormat(nexiaPlayer, component));
            }

        } catch (Exception ignored) {}
    }

    @Inject(method = "broadcastMessage", at = @At("HEAD"), cancellable = true)
    private void handleBotMessages(Component component, ChatType chatType, UUID uUID, CallbackInfo ci) {
        String key = ((TranslatableComponent) component).getKey();

        if (key.contains("multiplayer.player.left")) {
            if(leavePlayer.getTags().contains("bot")) ci.cancel();
            if(leavePlayer.getTags().contains("viafabricplus")) {
                leavePlayer.removeTag("viafabricplus");
                ci.cancel();
            }
            return;
        }

        if(key.contains("multiplayer.player.join")) {
            if(joinPlayer.getTags().contains("bot")) ci.cancel();
            if(((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(joinPlayer.getUUID())).clientType.equals(CorePlayerData.ClientType.VIAFABRICPLUS)) {
                joinPlayer.addTag("viafabricplus");
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "respawn")
    private void respawned(ServerPlayer oldPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer(oldPlayer);

        World respawn = nexiaPlayer.getRespawnPosition().getWorld();

        if(FfaSkyUtil.isFfaPlayer(nexiaPlayer)) {
            FfaSkyUtil.joinOrRespawn(nexiaPlayer);
            return;
        }

        if(respawn != null && LobbyUtil.isLobbyWorld(respawn)) {
            nexiaPlayer.getInventory().clear();
            LobbyUtil.giveItems(nexiaPlayer);
            nexiaPlayer.setGameMode(Minecraft.GameMode.ADVENTURE);

            nexiaPlayer.runCommand("/hub", 0, false);
            return;
        }

        if (BwUtil.isInBedWars(nexiaPlayer)) { BwPlayerEvents.respawned(nexiaPlayer); }
        if (SkywarsGame.world.equals(respawn) || SkywarsGame.isSkywarsPlayer(nexiaPlayer)) { nexiaPlayer.setGameMode(Minecraft.GameMode.SPECTATOR); }
    }

    @Unique
    private static Component joinFormat(Component original, ServerPlayer joinPlayer) {
        String name = joinPlayer.getScoreboardName();
        if(name.isEmpty()) { return original; }

        if(joinPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) {
            return ObjectMappings.convertComponent(
                    net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                            .append(net.kyori.adventure.text.Component.text("!").color(ChatFormat.goldColor)
                                    .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                                    .append(net.kyori.adventure.text.Component.text(name).color(ChatFormat.goldColor)))
            );
        } else {
            return ObjectMappings.convertComponent(
                    net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                            .append(net.kyori.adventure.text.Component.text("+").color(ChatFormat.greenColor))
                            .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                            .append(net.kyori.adventure.text.Component.text(name).color(ChatFormat.greenColor))
            );
        }
    }

    @Unique
    private static Component leaveFormat(Component original, ServerPlayer leavePlayer) {
        String name = leavePlayer.getScoreboardName();
        if(name.isEmpty()) { return original; }

        return ObjectMappings.convertComponent(
                net.kyori.adventure.text.Component.text("[").color(ChatFormat.lineColor)
                        .append(net.kyori.adventure.text.Component.text("-").color(ChatFormat.failColor)
                                .append(net.kyori.adventure.text.Component.text("] ").color(ChatFormat.lineColor))
                                .append(net.kyori.adventure.text.Component.text(name).color(ChatFormat.failColor)))
        );
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
                cir.setReturnValue(new TextComponent("§c§lYou have been banned.\n§7Duration: §d" + textBanTime + "\n§7Reason: §d" + reason + "\n§7You can appeal your ban at §d" + NexiaDiscord.config.discordLink));
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
    private static Component chatFormat(NexiaPlayer player, Component original) {
        try {
            TranslatableComponent component = (TranslatableComponent) original;
            Object[] args = component.getArgs();

            MutableComponent name = (MutableComponent) args[0];
            MutableComponent suffix = new TextComponent(" » ").withStyle(ChatFormatting.GRAY);

            String messageString = (String) args[1];
            MutableComponent message = new TextComponent(messageString).withStyle(ChatFormatting.WHITE);

            if(player.hasPermission("nexia.chat.formatting", 4)) {
                message = (MutableComponent) ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("§f").append(MineDown.parse(messageString)));
            }

            return name.append(suffix).append(message);

        } catch (Exception e) {
            return original;
        }
    }
}
