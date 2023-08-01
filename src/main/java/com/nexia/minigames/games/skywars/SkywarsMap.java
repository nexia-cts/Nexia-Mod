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
import java.util.List;

public class SkywarsMap {

    public static List<SkywarsMap> skywarsMaps = new ArrayList<>();

    public static List<String> stringSkywarsMaps = new ArrayList<>();


    public String id;

    public int maxPlayers;

    public ArrayList<EntityPos> positions;

    public StructureMap structureMap;

    public static SkywarsMap RELIC;


    public static SkywarsMap identifyMap(String name) {
        for(SkywarsMap map : SkywarsMap.skywarsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public static void createGlassBox(ServerLevel level, EntityPos pos) {
        BlockVec3 posC1 = pos.toBlockVec3().add(1, 2, 1);
        BlockVec3 posC2 = pos.toBlockVec3().add(-1, -1, -1);

        for (BlockPos pos1 : BlockPos.betweenClosed(
                posC1.x, posC1.y, posC1.z,
                posC2.x, posC2.y, posC2.z)) {

            if (level.getBlockState(pos1).getBlock() == Blocks.AIR && (
                    posC1.x == pos1.getX() || posC2.x == pos1.getX() ||
                            posC1.y == pos1.getY() || posC2.y == pos1.getY() ||
                            posC1.z == pos1.getZ() || posC2.z == pos1.getZ() )) {

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

    public SkywarsMap(String id, int maxPlayers, ArrayList<EntityPos> positions, StructureMap structureMap) {
        this.id = id;
        this.positions = positions;
        this.maxPlayers = maxPlayers;
        this.structureMap = structureMap;

        SkywarsMap.skywarsMaps.add(this);
        SkywarsMap.stringSkywarsMaps.add(id);
    }

    static {
        ArrayList<EntityPos> positions = new ArrayList<>();

        positions.add(new EntityPos(9,93,41));
        positions.add(new EntityPos(-19,93,41));
        positions.add(new EntityPos(-41,93,9));
        positions.add(new EntityPos(-41,93,-9));
        positions.add(new EntityPos(-9,93,-41));
        positions.add(new EntityPos(41,93,-9));
        positions.add(new EntityPos(9,93,-41));
        positions.add(new EntityPos(41,93,9));

        SkywarsMap.RELIC = new SkywarsMap("relic", 8, positions, new StructureMap(new Identifier("skywars", "relic"), Rotation.NO_ROTATION, true, new BlockPos(0, 70, 0), new BlockPos(0, 0, 0), true));

        positions.clear();
    }
}