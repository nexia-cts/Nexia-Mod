package com.nexia.core.mixin.item;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.players.BedwarsPlayerEvents;
import com.nexia.minigames.games.football.FootballGame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "place", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void beforePlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ServerPlayer player = (ServerPlayer)context.getPlayer();
        if (player == null) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        BlockPos blockPos = context.getClickedPos();
        ServerLevel level = player.getLevel();

        if (BedwarsAreas.isBedWarsWorld(player.getLevel()) && !BedwarsPlayerEvents.beforePlace(nexiaPlayer, context)) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
            return;
        }

        if (player.getLevel().equals(FootballGame.world) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
            return;
        }

        if (FfaUhcUtil.INSTANCE.isFfaWorld(level) && !FfaUhcUtil.INSTANCE.beforeBuild(nexiaPlayer, blockPos)) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
            return;
        }

        if (FfaSkyUtil.INSTANCE.isFfaWorld(level) && !FfaSkyUtil.INSTANCE.beforeBuild(nexiaPlayer, blockPos)) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
        }
    }

    @Inject(method = "place", at = @At(value = "TAIL"))
    private void afterPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ServerPlayer player = (ServerPlayer)context.getPlayer();
        if (player == null) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        BlockPos blockPos = context.getClickedPos();

        if (FfaSkyUtil.INSTANCE.isFfaWorld(player.getLevel())) {
            FfaSkyUtil.afterPlace(nexiaPlayer, blockPos, context.getHand());
        }
    }
}
