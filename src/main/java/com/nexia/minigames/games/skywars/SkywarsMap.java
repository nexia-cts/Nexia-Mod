package com.nexia.minigames.games.skywars;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.world.structure.Rotation;
import com.nexia.world.structure.StructureMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkywarsMap {

    public static List<SkywarsMap> skywarsMaps = new ArrayList<>();

    public static List<String> stringSkywarsMaps = new ArrayList<>();


    public String id;

    public int maxPlayers;

    public ArrayList<EntityPos> positions;

    private static BlockVec3 queueC1 = new EntityPos(0, 128, 0).toBlockVec3().add(-7, -1, -7);
    private static BlockVec3 queueC2 = new EntityPos(0, 128, 0).toBlockVec3().add(7, 6, 7);


    public StructureMap structureMap;

    public static SkywarsMap RELIC = new SkywarsMap("relic", 8, new ArrayList<>(Arrays.asList(
            new EntityPos(11,92,39),
            new EntityPos(-11,92,39),
            new EntityPos(-39,92,11),
            new EntityPos(-39,92,-11),
            new EntityPos(-11,92,-39),
            new EntityPos(11,92,-39),
            new EntityPos(39,92,-11),
            new EntityPos(39,92,11))
    ), new StructureMap(new Identifier("skywars", "relic"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-43, -7, -43), true));

    public static SkywarsMap SKYHENGE = new SkywarsMap("skyhenge", 12, new ArrayList<>(Arrays.asList(
            new EntityPos(72, 82, 0),
            new EntityPos(49,82,-23),
            new EntityPos(23,82,-49),
            new EntityPos(0,82,-72),
            new EntityPos(-23,82,-49),
            new EntityPos(-49,82,-23),
            new EntityPos(-72,82,0),
            new EntityPos(-49,82,23),
            new EntityPos(-23,82,49),
            new EntityPos(0,82,72),
            new EntityPos(23,82,49),
            new EntityPos(49,82,23))
    ), new StructureMap(new Identifier("skywars", "skyhenge"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-77, -7, -77), true));


    public static SkywarsMap identifyMap(String name) {
        for(SkywarsMap map : SkywarsMap.skywarsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public static void spawnQueueBuild(ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(
                queueC1.x, queueC1.y, queueC1.z,
                queueC2.x, queueC2.y, queueC2.z)) {

            if (level.getBlockState(pos).getBlock() == Blocks.AIR && (
                    queueC1.x == pos.getX() || queueC2.x == pos.getX() ||
                            queueC1.y == pos.getY() || queueC2.y == pos.getY() ||
                            queueC1.z == pos.getZ() || queueC2.z == pos.getZ() )) {

                level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
            }
        }
    }

    public SkywarsMap(String id, int maxPlayers, ArrayList<EntityPos> positions, StructureMap structureMap) {
        this.id = id;
        this.positions = positions;
        this.maxPlayers = maxPlayers;
        this.structureMap = structureMap;

        SkywarsMap.skywarsMaps.add(this);
        SkywarsMap.stringSkywarsMaps.add(id);
    }
}