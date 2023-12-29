package com.nexia.core.mixin.block;

import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {

    // nevermind i give up

    /*
    @Redirect(method = "spreadTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean uhcffathing(LevelAccessor instance, BlockPos blockPos, BlockState blockState, int i) {
        LavaFluid fluid = (LavaFluid) (Object) this;

        if(FfaUhcBlocks.getBlock(fluid.getSource().defaultFluidState().createLegacyBlock().))

        fluid.getSource()

        if(!FfaAreas.ffaWorld.getBlockState(blockPos).equals(blockState)) return false;


        return false;
    }

     */

    /*
    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (direction == Direction.DOWN) {
            FluidState fluidState2 = levelAccessor.getFluidState(blockPos);
            if (this.is(FluidTags.LAVA) && fluidState2.is(FluidTags.WATER)) {
                if (blockState.getBlock() instanceof LiquidBlock) {
                    levelAccessor.setBlock(blockPos, Blocks.STONE.defaultBlockState(), 3);
                }

                this.fizz(levelAccessor, blockPos);
                return;
            }
        }

        super.spreadTo(levelAccessor, blockPos, blockState, direction, fluidState);
    }

     */

}
