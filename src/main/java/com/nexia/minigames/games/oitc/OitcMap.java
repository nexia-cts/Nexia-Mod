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

    public static OitcMap CITY = new OitcMap("city", "City", 8, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0),
            new ArrayList<>(Arrays.asList(
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0),
                    new EntityPos(0, 80, 0,0,0)
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