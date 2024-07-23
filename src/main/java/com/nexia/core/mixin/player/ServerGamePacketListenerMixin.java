package com.nexia.core.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
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
import me.lucko.fabric.api.permissions.v0.Permissions;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChat", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void handleChat(ServerboundChatPacket serverboundChatPacket, CallbackInfo ci) {
        if (PlayerMutes.muted(new NexiaPlayer(player))) {
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

    /*
    @Inject(method = "handleCustomPayload", cancellable = true, at = @At("HEAD"))
    private void onCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket, CallbackInfo ci) {
        String brand = serverboundCustomPayloadPacket.data.readUtf(32767);
        player.sendMessage(LegacyChatFormat.format("Your brand: {}", brand), Util.NIL_UUID);
    }
     */

    @ModifyExpressionValue(
            method = "handleSetCommandBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z"
            )
    )
    public boolean canUseCommandBlock(boolean original) {
        return Permissions.check(this.player, "nexia.dev.commandblock");
    }

    @Inject(at = @At("HEAD"), method = "onDisconnect")
    private void getLeavePlayer(Component component, CallbackInfo ci) {
        ServerTime.leavePlayer = player;
    }

    @Inject(method = "handleUseItemOn", at = @At("HEAD"), cancellable = true)
    private void handleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if (BwUtil.isInBedWars(nexiaPlayer)) {
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
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if (BwUtil.isInBedWars(nexiaPlayer)) {
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

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if ((clickPacket.getClickType() == ClickType.THROW || slot == -999)) {
            if (!EventUtil.dropItem(nexiaPlayer, itemStack)) {
                ci.cancel();
                nexiaPlayer.refreshInventory();
                return;
            }
        }

        if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            if (!BwPlayerEvents.containerClick(nexiaPlayer, clickPacket)) {
                ci.cancel();
                nexiaPlayer.refreshInventory();
                return;
            }
        }

        if (FfaUtil.isFfaPlayer(nexiaPlayer) || BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            // If clicks on crafting slot
            if (containerId == 0 && slot >= 1 && slot <= 4) {
                ci.cancel();
                nexiaPlayer.refreshInventory();
                return;
            }
        }

    }

    @Inject(method = "handlePlaceRecipe", cancellable = true, at = @At("HEAD"))
    private void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundPlaceRecipePacket, CallbackInfo ci) {
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if (!EventUtil.craftItem(nexiaPlayer)) {
            ci.cancel();
            nexiaPlayer.refreshInventory();
            return;
        }
    }

    @Inject(method = "handlePlayerAction", cancellable = true, at = @At("HEAD"))
    private void handlePlayerAction(ServerboundPlayerActionPacket actionPacket, CallbackInfo ci) {
        ServerboundPlayerActionPacket.Action action = actionPacket.getAction();
        Inventory inv = player.inventory;

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if ((action == ServerboundPlayerActionPacket.Action.DROP_ITEM ||
                action == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) &&
                !EventUtil.dropItem(nexiaPlayer, inv.getItem(player.inventory.selected))) {
            player.connection.send(new ClientboundContainerSetSlotPacket(0, 36 + inv.selected, inv.getSelected()));
            ci.cancel();
            return;
        }
    }

    @Inject(method = "handleTeleportToEntityPacket", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"))
    private void handleSpectatorTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo ci) {

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);
        net.kyori.adventure.text.Component noSpectateMSG = net.kyori.adventure.text.Component.text("You can't spectate players in other games.").color(ChatFormat.failColor);

        if (BwUtil.isInBedWars(nexiaPlayer)) {
            if (!BwPlayerEvents.spectatorTeleport(nexiaPlayer, packet)) {
                ci.cancel();
                return;
            }
        }

        if (PlayerDataManager.get(nexiaPlayer).gameMode == PlayerGameMode.LOBBY) {
            ci.cancel();
            return;
        }

        if (OitcGame.isOITCPlayer(nexiaPlayer)) {
            ci.cancel();
            return;
        }

        for (ServerLevel serverLevel : ServerTime.minecraftServer.getAllLevels()) {
            Entity entity = packet.getEntity(serverLevel);
            if (!(entity instanceof ServerPlayer target)) continue;
            NexiaPlayer nexiaTarget = new NexiaPlayer(target);

            boolean cancel = (
                    FfaClassicUtil.isFfaPlayer(nexiaPlayer) && !FfaClassicUtil.isFfaPlayer(nexiaTarget)
            ) || (
                    FfaKitsUtil.isFfaPlayer(nexiaPlayer) && !FfaKitsUtil.isFfaPlayer(nexiaTarget)
            ) || (
                    FfaSkyUtil.isFfaPlayer(nexiaPlayer) && !FfaSkyUtil.isFfaPlayer(nexiaTarget)
            ) || (
                    FfaUhcUtil.isFfaPlayer(nexiaPlayer) && !FfaUhcUtil.isFfaPlayer(nexiaTarget)
            ) || (
                    SkywarsGame.isSkywarsPlayer(nexiaPlayer) && !SkywarsGame.isSkywarsPlayer(nexiaTarget)
            ) || (
                    FootballGame.isFootballPlayer(nexiaTarget) && !FootballGame.isFootballPlayer(nexiaTarget)
            );


            if (cancel) {
                nexiaPlayer.sendMessage(noSpectateMSG);
                ci.cancel();
                return;
            }
        }
    }
}
