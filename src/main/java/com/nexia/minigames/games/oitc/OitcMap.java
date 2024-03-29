package com.nexia.minigames.games.oitc;

import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OitcMap {

    public static List<OitcMap> oitcMaps = new ArrayList<>();

    public String id;

    public String name;

    public BlockPos corner1;

    public BlockPos corner2;

    public int maxPlayers;
    public ArrayList<EntityPos> spawnPositions;

    public static OitcMap JUNGLE_PLAZA = new OitcMap("jungle_plaza", "Jungle Plaza", 12, new BlockPos(36, 70, 36), new BlockPos(-36, 93, -36),
            new ArrayList<>(Arrays.asList(
                    new EntityPos(11.5, 77.0, 11.5,135,0),
                    new EntityPos(-10.5, 77.0, 11.5,215,0),
                    new EntityPos(-10.5, 77.0, -10.5,-45,0),
                    new EntityPos(-11.5, 77.0, -10.5,45,0),
                    new EntityPos(33.5, 77.0, 33.5,135,0),
                    new EntityPos(-32.5, 77.0, 33.5,-135,0),
                    new EntityPos(-32.5, 77.0, -32.5,-45,0),
                    new EntityPos(33.5, 77.0, -32.5,45,0),
                    new EntityPos(11.5, 77.0, -10.5,45,0),
                    new EntityPos(22, 77.0, 0,90,0),
                    new EntityPos(-22, 77.0, 0,-90, 0),
                    new EntityPos(0, 77.0, -22,0,0),
                    new EntityPos(0, 80.0, 0,0,0)
            ))
    );


    public static OitcMap identifyMap(String name) {
        for(OitcMap map : OitcMap.oitcMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public OitcMap(String id, String name, int maxPlayers, BlockPos corner1, BlockPos corner2, ArrayList<EntityPos> spawnPositions) {
        this.id = id;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.spawnPositions = spawnPositions;

        this.corner1 = corner1;
        this.corner2 = corner2;

        OitcMap.oitcMaps.add(this);
    }
}