package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.minecraft.core.BlockPos;

public class VanillaKitRoom extends KitRoom {
    public VanillaKitRoom(NexiaPlayer player) {
        super(player);

        this.setKitRoom(
                new StructureMap(
                        new Identifier("duels", "kitroom_vanilla"),
                        StructureMap.Rotation.NO_ROTATION,
                        true,
                        new BlockPos(0, 80, 0),
                        new BlockPos(-5, -1, -5),
                        true
                )
        );
    }
}

