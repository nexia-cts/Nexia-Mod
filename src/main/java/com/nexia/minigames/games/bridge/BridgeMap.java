package com.nexia.minigames.games.bridge;

import com.nexia.core.utilities.pos.EntityPos;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BridgeMap {

    public static List<BridgeMap> bridgeMaps = new ArrayList<>();

    public String id;

    public String name;

    public int maxGoals;

    public BlockPos corner1;

    public BlockPos corner2;

    public BlockPos team1goalCorner1;

    public BlockPos team1goalCorner2;

    public BlockPos team2goalCorner1;

    public BlockPos team2goalCorner2;

    public EntityPos team1Pos;
    public EntityPos team2Pos;

    public static BridgeMap STADIUM = new BridgeMap("stadium", "Stadium", 5, new BlockPos(68, 106, 86), new BlockPos(-68, 73, -86), new EntityPos(0, 80, -10), new EntityPos(0, 80, 10, -180, 0), new BlockPos(-3, 82, -52), new BlockPos(3, 79, -50),  new BlockPos(3, 82, 52),  new BlockPos(-3, 79, 50));


    public static BridgeMap identifyMap(String name) {
        for(BridgeMap map : BridgeMap.bridgeMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public BridgeMap(String id, String name, int maxGoals, BlockPos corner1, BlockPos corner2, EntityPos team1Pos, EntityPos team2Pos, BlockPos team1goalCorner1, BlockPos team1goalCorner2, BlockPos team2goalCorner1, BlockPos team2goalCorner2) {
        this.id = id;
        this.name = name;
        this.maxGoals = maxGoals;

        this.team1Pos = team1Pos;
        this.team2Pos = team2Pos;

        this.corner1 = corner1;
        this.corner2 = corner2;

        this.team1goalCorner1 = team1goalCorner1;
        this.team1goalCorner2 = team1goalCorner2;

        this.team2goalCorner1 = team2goalCorner1;
        this.team2goalCorner2 = team2goalCorner2;

        BridgeMap.bridgeMaps.add(this);
    }
}