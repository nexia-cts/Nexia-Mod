package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.minecraft.core.BlockPos;

public class CustomKitRoom extends KitRoom {
    public CustomKitRoom(NexiaPlayer player) {
        super(player);

        this.setKitRoom(
                new StructureMap(
                        new Identifier("duels", "kitroom_custom"),
                        StructureMap.Rotation.NO_ROTATION,
                        true,
                        new BlockPos(0, 78, 0),
                        new BlockPos(-22, -1, -11),
                        true
                )
        );
    }
}

