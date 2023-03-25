package com.nexia.core.mixin.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(StructureBlockEntity.class)
public class StructureBlockEntityMixin extends BlockEntity {
    @Shadow private String author;

    @Shadow private float integrity;

    @Shadow private Mirror mirror;

    @Shadow private Rotation rotation;

    @Shadow private long seed;

    @Shadow private BlockPos structurePos;

    @Shadow private BlockPos structureSize;

    @Shadow private boolean ignoreEntities;

    public StructureBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    private static Random createRandom(long l) {
        return l == 0L ? new Random(Util.getMillis()) : new Random(l);
    }

    @Redirect(method = "loadStructure(Lnet/minecraft/server/level/ServerLevel;ZLnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Z", at = @At("HEAD"))
    public boolean loadStructure(ServerLevel serverLevel, boolean bl, StructureTemplate structureTemplate) {
        BlockPos blockPos = this.getBlockPos();
        if (!StringUtil.isNullOrEmpty(structureTemplate.getAuthor())) {
            this.author = structureTemplate.getAuthor();
        }

        BlockPos blockPos2 = structureTemplate.getSize();
        boolean bl2 = this.structureSize.equals(blockPos2);
        if (!bl2) {
            this.structureSize = blockPos2;
            this.setChanged();
            BlockState blockState = serverLevel.getBlockState(blockPos);
            serverLevel.sendBlockUpdated(blockPos, blockState, blockState, 3);
        }

        if (bl && !bl2) {
            return false;
        } else {
            StructurePlaceSettings structurePlaceSettings = (new StructurePlaceSettings()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setChunkPos((ChunkPos)null);
            if (this.integrity < 1.0F) {
                structurePlaceSettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
            }

            BlockPos blockPos3 = blockPos.offset(this.structurePos);
            structureTemplate.placeInWorldChunk(serverLevel, blockPos3, structurePlaceSettings, createRandom(this.seed));
            return true;
        }
    }
}
