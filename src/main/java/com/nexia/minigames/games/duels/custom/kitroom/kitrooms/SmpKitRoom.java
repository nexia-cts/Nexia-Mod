package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.nexia.nexus.api.util.Identifier;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.world.StructureMap;
import net.minecraft.core.BlockPos;
import net.notcoded.codelib.util.world.structure.Rotation;

public class SmpKitRoom extends KitRoom {
    public SmpKitRoom(NexiaPlayer player) {
        super(player);

        this.setKitRoom(
                new StructureMap(
                        new Identifier("duels", "kitroom_smp"),
                        Rotation.NO_ROTATION,
                        true,
                        new BlockPos(0, 80, 0),
                        new BlockPos(-9, -1, -9),
                        true
                )
        );
    }
}

