package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.combatreforged.metis.api.util.Identifier;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.world.StructureMap;
import net.minecraft.core.BlockPos;
import net.notcoded.codelib.util.world.structure.Rotation;

public class CustomKitRoom extends KitRoom {
    public CustomKitRoom(NexiaPlayer player) {
        super(player);

        this.setKitRoom(
                new StructureMap(
                        new Identifier("duels", "kitroom_custom"),
                        Rotation.NO_ROTATION,
                        true,
                        new BlockPos(0, 78, 0),
                        new BlockPos(-22, -1, -11),
                        true
                )
        );
    }
}

