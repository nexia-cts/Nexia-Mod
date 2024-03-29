package com.nexia.core.mixin.item;

import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.ffa.uhc.utilities.FfaAreas;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemMixin extends Item {

    @Shadow @Final private Fluid content;

    public BucketItemMixin(Item.Properties properties) {
        super(properties);
    }

    @Inject(method = "use", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BucketItem;emptyBucket(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private void beforePlace(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        HitResult hitResult = getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY : net.minecraft.world.level.ClipContext.Fluid.NONE);
        BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = level.getBlockState(blockPos);
        BlockPos blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? blockPos : blockPos2;

        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(FfaAreas.isFfaWorld(level) && !FfaUhcUtil.beforeBuild(serverPlayer, blockPos3)) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(interactionHand)));
            ItemStackUtil.sendInventoryRefreshPacket(serverPlayer);
        } else if(FfaAreas.isFfaWorld(level) && FfaUhcUtil.beforeBuild(serverPlayer, blockPos3)) {
            FfaUhcUtil.afterPlace(serverPlayer, blockPos3, player.getUsedItemHand());
        }
    }
}
