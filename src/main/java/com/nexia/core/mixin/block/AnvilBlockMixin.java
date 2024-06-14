package com.nexia.core.mixin.block;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {

    @Inject(method = "use", cancellable = true, at = @At("HEAD"))
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);
        PlayerData playerData = PlayerDataManager.get(nexiaPlayer);

        if (BwUtil.isInBedWars(serverPlayer) || (playerData.gameOptions != null && (playerData.gameOptions.duelsGame != null || playerData.gameOptions.teamDuelsGame != null || playerData.gameOptions.customDuelsGame != null || playerData.gameOptions.customTeamDuelsGame != null)) || LobbyUtil.isLobbyWorld(serverPlayer.getLevel()) || (FfaUtil.isFfaPlayer(player) && !serverPlayer.isCreative())) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
    }

}
