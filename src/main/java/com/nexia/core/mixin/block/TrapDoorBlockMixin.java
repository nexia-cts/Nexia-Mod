package com.nexia.core.mixin.block;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrapDoorBlock.class)
public class TrapDoorBlockMixin {
    @Inject(method = "use", cancellable = true, at = @At("HEAD"))
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

        if ((FfaUtil.isFfaPlayer(nexiaPlayer) || KitRoom.isInKitRoom(nexiaPlayer) || LobbyUtil.isLobbyWorld(nexiaPlayer.getWorld())) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
