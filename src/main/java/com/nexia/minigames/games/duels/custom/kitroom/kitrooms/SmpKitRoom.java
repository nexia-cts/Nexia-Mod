package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.world.structure.Rotation;
import net.notcoded.codelib.util.world.structure.StructureMap;

public class SmpKitRoom extends KitRoom {
    public SmpKitRoom(AccuratePlayer player) {
        super(player);

        this.setKitRoom(
                new StructureMap(
                        new ResourceLocation("duels", "kitroom_smp"),
                        Rotation.NO_ROTATION,
                        true,
                        new BlockPos(0, 80, 0),
                        new BlockPos(-9, -1, -9),
                        true
                )
        );
    }
}

