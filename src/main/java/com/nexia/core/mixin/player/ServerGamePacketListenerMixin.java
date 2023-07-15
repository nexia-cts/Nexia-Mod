package com.nexia.core.mixin.player;

import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaUtil;
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

    @Inject(method = "handleContainerClick", cancellable = true, at = @At("HEAD"))
    private void handleContainerClick(ServerboundContainerClickPacket clickPacket, CallbackInfo ci) {
        int containerId = clickPacket.getContainerId();
        int slot = clickPacket.getSlotNum();
        ItemStack itemStack = clickPacket.getItem();

        if ((clickPacket.getClickType() == ClickType.THROW || slot == -999)) {
            if (!EventUtil.dropItem(player, itemStack)) {
                ItemStackUtil.sendInventoryRefreshPacket(player);
                ci.cancel();
                return;
            }
        }

        if (FfaUtil.isFfaPlayer(player)) {
            // If clicks on crafting slot
            if (containerId == 0 && slot >= 1 && slot <= 4) {
                ItemStackUtil.sendInventoryRefreshPacket(player);
                ci.cancel();
                return;
            }
        }

    }


    @Inject(at = @At("HEAD"), method = "onDisconnect")
    private void getLeavePlayer(Component component, CallbackInfo ci) {
        ServerTime.leavePlayer = player;
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
        if(PlayerDataManager.get(player).gameMode == PlayerGameMode.LOBBY){
            ci.cancel();
            return;
        }

        if(FfaUtil.isFfaPlayer(player)) {
            for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
                Entity entity = packet.getEntity(serverLevel);
                if (!(entity instanceof ServerPlayer target)) continue;

                if (!FfaUtil.isFfaPlayer(target)) {
                    PlayerUtil.getFactoryPlayer(player).sendMessage(net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor));
                    return;
                }
            }
        }
    }
}
