package com.nexia.core.mixin.block;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {GrindstoneBlock.class, DispenserBlock.class, CraftingTableBlock.class, BeaconBlock.class, AnvilBlock.class, HopperBlock.class, FlowerPotBlock.class})
public class MultipleBlockMixins {

    @Inject(method = "use", cancellable = true, at = @At("HEAD"))
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(serverPlayer);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer);

        if ((FfaUtil.isFfaPlayer(nexiaPlayer) || KitRoom.isInKitRoom(nexiaPlayer) || BwUtil.isInBedWars(nexiaPlayer) || (playerData.gameOptions != null && (playerData.gameOptions.duelsGame != null || playerData.gameOptions.teamDuelsGame != null || playerData.gameOptions.customTeamDuelsGame != null || playerData.gameOptions.customDuelsGame != null)) || LobbyUtil.isLobbyWorld(nexiaPlayer.getWorld())) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

}