package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.minecraft.core.BlockPos;

public class SmpKitRoom extends KitRoom {
    public SmpKitRoom(NexiaPlayer player) {
        super(player);

        this.setKitRoom(
                new StructureMap(
                        new Identifier("duels", "kitroom_smp"),
                        StructureMap.Rotation.NO_ROTATION,
                        true,
                        new BlockPos(0, 80, 0),
                        new BlockPos(-9, -1, -9),
                        true
                )
        );
    }
}

