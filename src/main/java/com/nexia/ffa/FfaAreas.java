package com.nexia.ffa;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.pos.PositionUtil;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.util.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface FfaAreas {
    ServerLevel getFfaWorld();
    World getNexusFfaWorld();
    Location getFfaLocation();

    EntityPos getSpawn();
    AABB getSpawnCorners();

    AABB getFfaCorners();

    default boolean isInFfaSpawn(NexiaPlayer player) {
        return PositionUtil.isBetween(getSpawnCorners(), player.getLocation());
    }

    boolean isFfaWorld(Level level);
}
