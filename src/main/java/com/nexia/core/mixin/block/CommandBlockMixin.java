package com.nexia.core.mixin.block;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CommandBlock.class)
public class CommandBlockMixin extends BaseEntityBlock {

    protected CommandBlockMixin(Properties properties) {
        super(properties);
    }

    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        CommandBlockEntity commandBlockEntity = new CommandBlockEntity();
        commandBlockEntity.setAutomatic(this == Blocks.CHAIN_COMMAND_BLOCK);
        return commandBlockEntity;
    }

    /**
     * @author NotCoded
     * @reason Modify command block opening behaviour.
     */
    @Overwrite
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if(!Permissions.check(player, "nexia.dev.commandblock")) return InteractionResult.FAIL;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity && player.canUseGameMasterBlocks()) {
            player.openCommandBlock((CommandBlockEntity)blockEntity);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
}