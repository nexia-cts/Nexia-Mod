package com.nexia.core.mixin.block;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneBlock.class)
public class GrindstoneBlockMixin {

    @Inject(method = "use", cancellable = true, at = @At("HEAD"))
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        PlayerData playerData = PlayerDataManager.get(serverPlayer);

        if ((playerData.duelsGame != null || playerData.teamDuelsGame != null) || LobbyUtil.isLobbyWorld(serverPlayer.getLevel())) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
    }

}