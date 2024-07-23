package com.nexia.core.mixin.item;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaAreas;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
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

        if (BwAreas.isBedWarsWorld(player.getLevel()) && !BwPlayerEvents.beforePlace(nexiaPlayer, context)) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
            return;
        }

        if (player.getLevel().equals(FootballGame.world) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
            return;
        }

        if (FfaAreas.isFfaWorld(level) && !FfaUhcUtil.beforeBuild(nexiaPlayer, blockPos)) {
            cir.setReturnValue(InteractionResult.PASS);
            nexiaPlayer.refreshInventory();
            return;
        }

        if (com.nexia.ffa.sky.utilities.FfaAreas.isFfaWorld(level) && !FfaSkyUtil.beforeBuild(nexiaPlayer, blockPos)) {
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

        if (com.nexia.ffa.sky.utilities.FfaAreas.isFfaWorld(player.getLevel())) {
            FfaSkyUtil.afterPlace(nexiaPlayer, blockPos, context.getHand());
        }
    }
}
