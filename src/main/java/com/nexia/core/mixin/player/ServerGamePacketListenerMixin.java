package com.nexia.core.mixin.player;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void handleChat(ServerboundChatPacket serverboundChatPacket, CallbackInfo ci) {
        if (PlayerMutes.muted(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", cancellable = true, at = @At("HEAD"))
    private void mobInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
        ServerLevel level = player.getLevel();

        if (BwAreas.isBedWarsWorld(level) && !BwPlayerEvents.interact(player, packet)) {
            ci.cancel();

        }
    }

    // Thank you, our lord and saviour
    //   _____  _          _____            _
    // |  __ \(_)        / ____|          | |
    // | |__) |_ _______| |     ___   ___ | | _____ _   _
    // |  _  /| |_  / _ \ |    / _ \ / _ \| |/ / _ \ | | |
    // | | \ \| |/ /  __/ |___| (_) | (_) |   <  __/ |_| |
    // |_|  \_\_/___\___|\_____\___/ \___/|_|\_\___|\__, |
    //                                               __/ |
    //                                              |___/
    @Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getCurrentAttackReach(F)F"))
    public float redirectReachLonger(ServerPlayer playerEntity, float f) {
        return playerEntity.getCurrentAttackReach(f) + 0.75F;
    }

    @Inject(at = @At("HEAD"), method = "onDisconnect")
    private void getLeavePlayer(Component component, CallbackInfo ci) {
        ServerTime.leavePlayer = player;
    }

    @Inject(method = "handleUseItemOn", at = @At("HEAD"), cancellable = true)
    private void handleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {

        if (BwUtil.isInBedWars(player)) {
            if (!BwPlayerEvents.useItem(player, packet.getHand())) {
                ci.cancel();
                BlockPos blockPos = packet.getHitResult().getBlockPos().relative(packet.getHitResult().getDirection());
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, player.level.getBlockState(blockPos)));
                return;
            }
        }

    }

    @Inject(method = "handleUseItem", at = @At("HEAD"), cancellable = true)
    private void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket, CallbackInfo ci) {

        if (BwUtil.isInBedWars(player)) {
            if (!BwPlayerEvents.useItem(player, serverboundUseItemPacket.getHand())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleContainerClick", cancellable = true, at = @At("HEAD"))
    private void handleContainerClick(ServerboundContainerClickPacket clickPacket, CallbackInfo ci) {
        int containerId = clickPacket.getContainerId();
        int slot = clickPacket.getSlotNum();
        ItemStack itemStack = clickPacket.getItem();

        if ((clickPacket.getClickType() == ClickType.THROW || slot == -999)) {
            if (!EventUtil.dropItem(player, itemStack)) {
                ci.cancel();
                ItemStackUtil.sendInventoryRefreshPacket(player);
                return;
            }
        }

        if (BwUtil.isBedWarsPlayer(player)) {
            if (!BwPlayerEvents.containerClick(player, clickPacket)) {
                ci.cancel();
                ItemStackUtil.sendInventoryRefreshPacket(player);
                return;
            }
        }

        if (FfaUtil.isFfaPlayer(player)) {
            // If clicks on crafting slot
            if (containerId == 0 && slot >= 1 && slot <= 4) {
                ci.cancel();
                ItemStackUtil.sendInventoryRefreshPacket(player);
                return;
            }
        }

    }

    @Inject(method = "handlePlayerAction", cancellable = true, at = @At("HEAD"))
    private void handlePlayerAction(ServerboundPlayerActionPacket actionPacket, CallbackInfo ci) {
        ServerboundPlayerActionPacket.Action action = actionPacket.getAction();
        Inventory inv = player.inventory;

        if ((action == ServerboundPlayerActionPacket.Action.DROP_ITEM ||
                action == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) &&
                    !EventUtil.dropItem(player, inv.getItem(player.inventory.selected))) {
            player.connection.send(new ClientboundContainerSetSlotPacket(0, 36 + inv.selected, inv.getSelected()));
            ci.cancel();
            return;
        }
    }

    @Inject(method = "handleTeleportToEntityPacket", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"))
    private void handleSpectatorTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo ci) {

        if (BwUtil.isInBedWars(player)) {
            if (!BwPlayerEvents.spectatorTeleport(player, packet)) {
                ci.cancel();
                return;
            }
        }

        if(FfaClassicUtil.isFfaPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!FfaClassicUtil.isFfaPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    ci.cancel();
                    return;
                }
            }
        }

        if(FfaKitsUtil.isFfaPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!FfaKitsUtil.isFfaPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    ci.cancel();
                    return;
                }
            }
        }

        if(FfaSkyUtil.isFfaPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!FfaSkyUtil.isFfaPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    ci.cancel();
                    return;
                }
            }
        }

        if(FfaUhcUtil.isFfaPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!FfaUhcUtil.isFfaPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    ci.cancel();
                    return;
                }
            }
        }

        if(SkywarsGame.isSkywarsPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!SkywarsGame.isSkywarsPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    ci.cancel();
                    return;
                }
            }
        }

        if(FootballGame.isFootballPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!FootballGame.isFootballPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    ci.cancel();
                    return;
                }
            }
        }

        if(PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY){
            ci.cancel();
            return;
        }

        if(OitcGame.isOITCPlayer(player)){
            ci.cancel();
            return;
        }
    }
}
