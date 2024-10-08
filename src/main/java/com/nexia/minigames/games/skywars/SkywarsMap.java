package com.nexia.minigames.games.skywars;

import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkywarsMap {

    public static List<SkywarsMap> skywarsMaps = new ArrayList<>();

    public static List<String> stringSkywarsMaps = new ArrayList<>();


    public static List<SkywarsMap> fourPlayerMaps = new ArrayList<>();

    public static List<SkywarsMap> eightPlayerMaps = new ArrayList<>();

    public static List<SkywarsMap> twelvePlayerMaps = new ArrayList<>();

    public final String id;

    public final int maxPlayers;

    public final ArrayList<EntityPos> positions;

    private static final BlockVec3 queueC1 = new EntityPos(0, 128, 0).toBlockVec3().add(-7, -1, -7);
    private static final BlockVec3 queueC2 = new EntityPos(0, 128, 0).toBlockVec3().add(7, 6, 7);

    public static final int maxJoinablePlayers = 12;

    public StructureMap structureMap;

    public static SkywarsMap RELIC = new SkywarsMap("relic", 8, new ArrayList<>(Arrays.asList(
            new EntityPos(59.5,91.0,-14.5),
            new EntityPos(59.5,91.0,14.5),
            new EntityPos(14.5,91.0,59.5),
            new EntityPos(-14.5,91.0,59.5),
            new EntityPos(-59.5,91.0,14.5),
            new EntityPos(-59.5,91.0,-14.5),
            new EntityPos(-14.5,91.0,-59.5),
            new EntityPos(14.5,91.0,-59.5))
    ), new StructureMap(new Identifier("skywars", "relic"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-63,-7,-63), true));

    public static SkywarsMap SKYHENGE = new SkywarsMap("skyhenge", 12, new ArrayList<>(Arrays.asList(
            new EntityPos(72.5, 88.0, 0.5),
            new EntityPos(49.5,88.0,-23.5),
            new EntityPos(23.5,88.0,-49.5),
            new EntityPos(0.5,88.0,-72.5),
            new EntityPos(-23.5,88.0,-49.5),
            new EntityPos(-49.5,88.0,-23.5),
            new EntityPos(-72.5,88.0,0.5),
            new EntityPos(-49.5,88.0,23.5),
            new EntityPos(-23.5,88.0,49.5),
            new EntityPos(0.5,88.0,72.5),
            new EntityPos(23.5,88.0,49.5),
            new EntityPos(49.5,88.0,23.5))
    ), new StructureMap(new Identifier("skywars", "skyhenge"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-77, -7, -77), true));

    public static SkywarsMap PLACEHOLDER = new SkywarsMap("placeholder", 12, null, null);

    public static SkywarsMap BELOW = new SkywarsMap("below", 8, new ArrayList<>(Arrays.asList(
            new EntityPos(-28.5, 81.0, 69.5),
            new EntityPos(28.5,81.0,69.5),
            new EntityPos(69.5,81.0,28.5),
            new EntityPos(69.5,81.0,-28.5),
            new EntityPos(29.5,81.0,-68.5),
            new EntityPos(-28.5,81.0,-68.5),
            new EntityPos(-68.5,81.0,-28.5),
            new EntityPos(-68.5,81.0,29.5))
    ), new StructureMap(new Identifier("skywars", "below"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-76, -9, -76), true));

    public static SkywarsMap NULL = new SkywarsMap("null", 8, new ArrayList<>(Arrays.asList(
            new EntityPos(25.5, 77.0, 25.5),
            new EntityPos(-24.5, 77.0, 25.5),
            new EntityPos(25.5, 77.0, -24.5),
            new EntityPos(-49.5, 77, 0.5),
            new EntityPos(0.5, 77, 50.5),
            new EntityPos(50.5, 77, 0.5),
            new EntityPos(0.5, 77, -49.5),
            new EntityPos(-24.5, 77.0, -24.5))
    ), new StructureMap(new Identifier("skywars", "null"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-54, -42, -54), true));


    public static SkywarsMap SHROOMS = new SkywarsMap("shrooms", 8, new ArrayList<>(Arrays.asList(
            new EntityPos(-22.5, 81.0, -75.5),
            new EntityPos(6.5, 81, -79.5),
            new EntityPos(28.5, 81, -69.5),
            new EntityPos(-76.5, 81, -9.5),
            new EntityPos(-76.5, 81, 14.5),
            new EntityPos(-17.5, 81, 75.5),
            new EntityPos(10.5, 81, 76.5),
            new EntityPos(36.5, 81, 68.5))
    ), new StructureMap(new Identifier("skywars", "shrooms"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-85, -20, -85), true));


    public static SkywarsMap identifyMap(String name) {
        for(SkywarsMap map : SkywarsMap.skywarsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public static SkywarsMap validateMap(SkywarsMap currentMap, int currentPlayers) {
        if(currentPlayers > currentMap.maxPlayers) {
            SkywarsMap fixedMap = calculateMap(currentPlayers, false);
            if(currentPlayers > fixedMap.maxPlayers) {
                fixedMap = SkywarsMap.twelvePlayerMaps.get(RandomUtil.randomInt(SkywarsMap.twelvePlayerMaps.size()));
            }
            return fixedMap;
        }

        return currentMap;
    }

    public static void spawnQueueBuild(ServerLevel level, boolean setAir) {
        for (BlockPos pos : BlockPos.betweenClosed(
                queueC1.x, queueC1.y, queueC1.z,
                queueC2.x, queueC2.y, queueC2.z)) {

            if(!setAir) {
                if (level.getBlockState(pos).getBlock() == Blocks.AIR && (
                        queueC1.x == pos.getX() || queueC2.x == pos.getX() ||
                                queueC1.y == pos.getY() || queueC2.y == pos.getY() ||
                                queueC1.z == pos.getZ() || queueC2.z == pos.getZ() )) {

                    level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
                }
            } else {
                if (level.getBlockState(pos).getBlock() == Blocks.GLASS && (
                        queueC1.x == pos.getX() || queueC2.x == pos.getX() ||
                                queueC1.y == pos.getY() || queueC2.y == pos.getY() ||
                                queueC1.z == pos.getZ() || queueC2.z == pos.getZ() )) {

                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }

        }
    }

    public static void deleteWorld(String id) {
        WorldUtil.deleteWorld(new Identifier("skywars", id));
    }

    public SkywarsMap(String id, int maxPlayers, ArrayList<EntityPos> positions, StructureMap structureMap) {
        this.id = id;
        this.positions = positions;
        this.maxPlayers = maxPlayers;
        this.structureMap = structureMap;

        if(this.id.equals("placeholder")) return;

        SkywarsMap.skywarsMaps.add(this);
        SkywarsMap.stringSkywarsMaps.add(id);

        if(maxPlayers == 8) SkywarsMap.eightPlayerMaps.add(this);
        if(maxPlayers == 12) SkywarsMap.twelvePlayerMaps.add(this);

        SkywarsMap.fourPlayerMaps.add(this);
    }

    public static SkywarsMap calculateMap(int players, boolean rerollPrevention) {
        if(players <= 4 && (rerollPrevention && !SkywarsMap.fourPlayerMaps.contains(SkywarsGame.map))) {
            return SkywarsMap.fourPlayerMaps.get(RandomUtil.randomInt(SkywarsMap.fourPlayerMaps.size()));
        }
        else if(players >= 5 && players <= 8 && (rerollPrevention && !SkywarsMap.eightPlayerMaps.contains(SkywarsGame.map))) {
            return SkywarsMap.eightPlayerMaps.get(RandomUtil.randomInt(SkywarsMap.eightPlayerMaps.size()));
        }
        else if(players >= 9 && (rerollPrevention && !SkywarsMap.twelvePlayerMaps.contains(SkywarsGame.map))) {
            return SkywarsMap.twelvePlayerMaps.get(RandomUtil.randomInt(SkywarsMap.twelvePlayerMaps.size()));
        }
        return SkywarsGame.map;
    }
}