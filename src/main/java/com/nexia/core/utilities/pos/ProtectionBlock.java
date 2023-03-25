package com.nexia.core.utilities.pos;

import net.minecraft.world.level.block.Block;

public class ProtectionBlock {
    public Block block;
    public boolean canBuild;
    public String noBuildMessage;

    public ProtectionBlock(Block block, boolean canBuild, String noBuildMessage) {
        this.block = block;
        this.canBuild = canBuild;
        this.noBuildMessage = noBuildMessage;
    }
}
