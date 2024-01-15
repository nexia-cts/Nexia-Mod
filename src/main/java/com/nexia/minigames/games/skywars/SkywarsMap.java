package com.nexia.minigames.games.skywars;

import com.nexia.core.Main;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.notcoded.codelib.util.world.structure.Rotation;
import net.notcoded.codelib.util.world.structure.StructureMap;
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;
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
    ), new StructureMap(new ResourceLocation("skywars", "relic"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-63,-7,-63), true));

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
    ), new StructureMap(new ResourceLocation("skywars", "skyhenge"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-77, -7, -77), true));

    public static SkywarsMap BELOW = new SkywarsMap("below", 8, new ArrayList<>(Arrays.asList(
            new EntityPos(-28.5, 81.0, 69.5),
            new EntityPos(28.5,81.0,69.5),
            new EntityPos(69.5,81.0,28.5),
            new EntityPos(69.5,81.0,-28.5),
            new EntityPos(29.5,81.0,-68.5),
            new EntityPos(-28.5,81.0,-68.5),
            new EntityPos(-68.5,81.0,-28.5),
            new EntityPos(-68.5,81.0,29.5))
    ), new StructureMap(new ResourceLocation("skywars", "below"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-76, -9, -76), true));

    public static SkywarsMap NULL = new SkywarsMap("null", 4, new ArrayList<>(Arrays.asList(
            new EntityPos(25.5, 77.0, 25.5),
            new EntityPos(-24.5, 77.0, 25.5),
            new EntityPos(25.5, 77.0, -24.5),
            new EntityPos(-24.5, 77.0, -24.5))
    ), new StructureMap(new ResourceLocation("skywars", "null"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-33, -42, -33), true));

    public static SkywarsMap SHROOMS = new SkywarsMap("shrooms", 4, new ArrayList<>(Arrays.asList(
            new EntityPos(40.5, 80, 40.5),
            new EntityPos(40.5, 80, -39.5),
            new EntityPos(-39.5, 80, -39.5),
            new EntityPos(-39.5, 80, 40.5))
    ), new StructureMap(new ResourceLocation("skywars", "shrooms"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-44, -6, -45), true));


    public static SkywarsMap identifyMap(String name) {
        for(SkywarsMap map : SkywarsMap.skywarsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
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
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("skywars", id)).location(),
                    null);
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/skywars", id));
            ServerTime.factoryServer.unloadWorld("skywars:" + id, false);
        } catch (Exception e) {
            Main.logger.error("Error occurred while deleting world: skywars:" + id);
            if(Main.config.debugMode) e.printStackTrace();
            try {
                ServerTime.factoryServer.unloadWorld("skywars:" + id, false);
            } catch (Exception e2) {
                if(Main.config.debugMode) e2.printStackTrace();
            }
            return;
        }
        worldHandle.delete();
    }

    public SkywarsMap(String id, int maxPlayers, ArrayList<EntityPos> positions, StructureMap structureMap) {
        this.id = id;
        this.positions = positions;
        this.maxPlayers = maxPlayers;
        this.structureMap = structureMap;

        SkywarsMap.skywarsMaps.add(this);
        SkywarsMap.stringSkywarsMaps.add(id);

        if(maxPlayers == 4) SkywarsMap.fourPlayerMaps.add(this);
        if(maxPlayers == 8) SkywarsMap.eightPlayerMaps.add(this);
        if(maxPlayers == 12) SkywarsMap.twelvePlayerMaps.add(this);
    }

    public static SkywarsMap calculateMap(int oldPlayers, int newPlayers) {
        if(newPlayers <= 4) {
            return SkywarsMap.fourPlayerMaps.get(RandomUtil.randomInt(SkywarsMap.fourPlayerMaps.size()));
        }
        if(newPlayers >= 5 && oldPlayers <= 4) {
            return SkywarsMap.eightPlayerMaps.get(RandomUtil.randomInt(SkywarsMap.eightPlayerMaps.size()));
        }
        if(newPlayers >= 9 && oldPlayers <= 8) {
            return SkywarsMap.twelvePlayerMaps.get(RandomUtil.randomInt(SkywarsMap.twelvePlayerMaps.size()));
        }
        return SkywarsMap.SKYHENGE;
    }
}
