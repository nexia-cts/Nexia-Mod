package com.nexia.minigames.games.skywars;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.world.structure.Rotation;
import com.nexia.world.structure.StructureMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            new EntityPos(-9,91,-41),
            new EntityPos(9,91,-41),
            new EntityPos(41,91,-9),
            new EntityPos(41,91,9),
            new EntityPos(9,91,41),
            new EntityPos(-9,91,41),
            new EntityPos(-41,91,9),
            new EntityPos(-41,91,-9))
    ), new StructureMap(new Identifier("skywars", "relic"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-45, -7, -45), true));


    public static SkywarsMap identifyMap(String name) {
        for(SkywarsMap map : SkywarsMap.skywarsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public static void createGlassBox(ServerLevel level, EntityPos pos) {
        BlockVec3 posC1 = pos.toBlockVec3().add(1, 2, 1);
        BlockVec3 posC2 = pos.toBlockVec3().add(-1, -1, -1);

        // for loop is crashing the server

        for (BlockPos pos1 : BlockPos.betweenClosed(
                posC1.x, posC1.y, posC1.z,
                posC2.x, posC2.y, posC2.z)) {
            if (posC1.x == pos1.getX() || posC2.x == pos1.getX() ||
                    posC1.y == pos1.getY() || posC2.y == pos1.getY() ||
                    posC1.z == pos1.getZ() || posC2.z == pos1.getZ()) {
                level.setBlock(pos1, Blocks.GLASS.defaultBlockState(), 3);
            }
        }

        level.setBlock(pos.toBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos.add(0, 1, 0).toBlockPos(), Blocks.AIR.defaultBlockState(), 3);
    }

    public static void removeGlassBox(ServerLevel level, EntityPos pos) {
        BlockVec3 posC1 = pos.toBlockVec3().add(1, 2, 1);
        BlockVec3 posC2 = pos.toBlockVec3().add(-1, -1, -1);

        for (BlockPos pos1 : BlockPos.betweenClosed(
                posC1.x, posC1.y, posC1.z,
                posC2.x, posC2.y, posC2.z)) {

            if (level.getBlockState(pos1).getBlock() != Blocks.AIR && (
                    posC1.x == pos1.getX() || posC2.x == pos1.getX() ||
                            posC1.y == pos1.getY() || posC2.y == pos1.getY() ||
                            posC1.z == pos1.getZ() || posC2.z == pos1.getZ() )) {

                level.setBlock(pos1, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public static void spawnQueueBuild(ServerLevel level) {

        // this doesnt crash it?

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