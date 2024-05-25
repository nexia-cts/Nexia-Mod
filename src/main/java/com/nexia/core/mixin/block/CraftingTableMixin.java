package com.nexia.core.mixin.block;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.FfaUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.notcoded.codelib.players.AccuratePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingTableBlock.class)
public class CraftingTableMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(serverPlayer));

        if((FfaUtil.isFfaPlayer(nexiaPlayer) || LobbyUtil.isLobbyWorld(player.level)) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
