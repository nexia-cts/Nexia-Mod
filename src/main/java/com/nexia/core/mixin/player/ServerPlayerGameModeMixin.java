package com.nexia.core.mixin.player;

import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaAreas;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow public ServerLevel level;

    @Unique
    private boolean isBed = false;

    @Inject(at = @At("HEAD"), method = "destroyBlock", cancellable = true)
    private void destroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {

        if (BwAreas.isBedWarsWorld(level) && !BwPlayerEvents.beforeBreakBlock(player, blockPos)) {
            cir.setReturnValue(false);
        }  else if (FfaAreas.isFfaWorld(level) && !FfaUhcUtil.beforeBuild(player, blockPos)) {
            cir.setReturnValue(false);
        } else if (com.nexia.ffa.sky.utilities.FfaAreas.isFfaWorld(level) && !FfaSkyUtil.beforeBuild(player, blockPos)) {
            cir.setReturnValue(false);
        }

        isBed = BlockUtil.blockToText(player.level.getBlockState(blockPos)).endsWith("_bed");
    }

    @Inject(at = @At("RETURN"), method = "destroyBlock")
    private void destroyBlockTail(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {

        if (BwUtil.isBedWarsPlayer(player) && isBed) {
            BwPlayerEvents.bedBroken(player, blockPos);
        }

    }

}
