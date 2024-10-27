package com.nexia.core.mixin.block;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.minigames.games.bridge.BridgeGame;
import com.nexia.minigames.games.bridge.util.player.BridgePlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.nexia.core.NexiaCore.BRIDGE_DATA_MANAGER;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @Inject(method = "entityInside",
            at = @At("HEAD")
    )
    private void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);

        if (nexiaPlayer.hasTag("in_bridge_game")) {

            BridgePlayerData data = (BridgePlayerData) PlayerDataManager.getDataManager(BRIDGE_DATA_MANAGER).get(nexiaPlayer);

            if (data.team == BridgeGame.team1) {

                BridgeGame.goal(nexiaPlayer, "team1");
            }
            if (data.team == BridgeGame.team2) {

                BridgeGame.goal(nexiaPlayer, "team2");
            }
        }
    }
}
