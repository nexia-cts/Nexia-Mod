package com.nexia.core.mixin.item;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.ffa.uhc.utilities.FfaAreas;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
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
        BlockPos blockPos = context.getClickedPos();
        ServerLevel level = player.getLevel();

        if (BwAreas.isBedWarsWorld(player.getLevel()) && !BwPlayerEvents.beforePlace(player, context)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (FfaAreas.isFfaWorld(level) && !FfaUhcUtil.beforeBuild(player, blockPos)) {
            cir.setReturnValue(InteractionResult.PASS);
            ItemStackUtil.sendInventoryRefreshPacket(player);
        }
    }

    @Inject(method = "place", at = @At(value = "TAIL"))
    private void afterPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ServerPlayer player = (ServerPlayer)context.getPlayer();
        if (player == null) return;
        BlockPos blockPos = context.getClickedPos();
        FfaUhcUtil.afterPlace(player, blockPos, context.getHand(), false);
    }
}
