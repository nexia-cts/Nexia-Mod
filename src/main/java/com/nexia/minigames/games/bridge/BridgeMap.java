package com.nexia.minigames.games.bridge;

import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BridgeMap {


    public static final BridgeMap NETHFLAT = new BridgeMap("Neth Flat", "Neth Flat", new EntityPos(0, 80, -41, 0, 0), new EntityPos(0, 80, 41, 180, 0), new StructureMap(new Identifier("duels", "nethflat"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-36, -3, -51), true));
    public static List<BridgeMap> bridgeMaps = new ArrayList<>();
    public String id;
    public String name;
    public EntityPos team1Pos;
    public EntityPos team2Pos;
    public StructureMap structureMap;


    public BridgeMap(String id, String name, EntityPos team1Pos, EntityPos team2Pos, StructureMap structureMap) {
        this.id = id;
        this.name = name;

        this.team1Pos = team1Pos;
        this.team2Pos = team2Pos;

        this.structureMap = structureMap;

        BridgeMap.bridgeMaps.add(this);
    }

    public static BridgeMap identifyMap(String name) {
        for (BridgeMap map : BridgeMap.bridgeMaps) {
            if (map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }


}