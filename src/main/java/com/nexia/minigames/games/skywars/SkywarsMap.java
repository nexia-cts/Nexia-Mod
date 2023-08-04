package com.nexia.minigames.games.skywars;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.Main;
import com.nexia.core.utilities.pos.BlockVec3;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.world.structure.Rotation;
import com.nexia.world.structure.StructureMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;
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
            new EntityPos(59,92,-14),
            new EntityPos(59,92,14),
            new EntityPos(14,92,59),
            new EntityPos(-14,92,59),
            new EntityPos(-59,92,14),
            new EntityPos(-59,92,-14),
            new EntityPos(-14,92,-59),
            new EntityPos(14,92,-59))
    ), new StructureMap(new Identifier("skywars", "relic"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-63,-7,-63), true));

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

    public static void deleteWorld(String id) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("skywars", id)).location(),
                    new RuntimeWorldConfig());
            ServerTime.factoryServer.unloadWorld("skywars:" + id, false);
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/skywars", id));
        } catch (Exception ignored) {
            Main.logger.error("Error occurred while deleting world: skywars:" + id);
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
    }
}