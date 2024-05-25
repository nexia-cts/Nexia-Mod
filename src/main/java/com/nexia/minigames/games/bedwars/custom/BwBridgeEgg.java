package com.nexia.minigames.games.bedwars.custom;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BwBridgeEgg extends ThrownEgg {

    public static final int throwTime = 2 * 20;
    public static final String itemTagKey = "BedWarsBridgeEgg";

    public EntityPos[] previousPositions;
    public int oldestPosIndex;

    public NexiaPlayer owner;
    public Block trailBlock;
    public int age;

    public BwBridgeEgg(Level level, NexiaPlayer player, Block trailBlock) {
        super(level, player.player().get());

        this.owner = player;
        this.trailBlock = trailBlock;
        this.age = 0;

        previousPositions = new EntityPos[3];
        oldestPosIndex = 0;
    }

    public void tick() {
        if (age >= throwTime) this.kill();
        super.tick();

        EntityPos oldestPos = previousPositions[oldestPosIndex];
        if (oldestPos != null) {
            for (BlockPos blockPos : getSurroundingBlocks(oldestPos)) {
                if (level.getBlockState(blockPos).getBlock() == Blocks.AIR) {
                    if (!BwAreas.canBuildAt(owner, blockPos, false)) continue;
                    level.setBlock(blockPos, trailBlock.defaultBlockState(), 3);
                }
            }
            playSound(SoundEvents.CHICKEN_EGG, 1f, 1f);
        }

        if (age > 0) {
            previousPositions[oldestPosIndex] = new EntityPos(this.position());
            oldestPosIndex++;
            if (oldestPosIndex >= previousPositions.length) oldestPosIndex = 0;
        }
        age++;
    }

    private BlockPos[] getSurroundingBlocks(EntityPos oldest) {
        return new BlockPos[]{
                oldest.c().add(0.5, 0, 0).toBlockPos(),
                oldest.c().add(-0.5, 0, 0).toBlockPos(),
                oldest.c().add(0, 0, 0.5).toBlockPos(),
                oldest.c().add(0, 0, -0.5).toBlockPos()
        };
    }

}
