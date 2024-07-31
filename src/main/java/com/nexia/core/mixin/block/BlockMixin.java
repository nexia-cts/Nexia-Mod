package com.nexia.core.mixin.block;

import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.minigames.games.bedwars.util.BedwarsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void dropResources(BlockState blockState, Level level, BlockPos blockPos, BlockEntity blockEntity, Entity breakerEntity, ItemStack tool, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if ((BedwarsAreas.isBedWarsWorld(serverLevel) && !(BedwarsUtil.dropResources(blockState))) || FfaSkyUtil.INSTANCE.isFfaWorld(serverLevel)) {
            ci.cancel();
        }
    }

}
