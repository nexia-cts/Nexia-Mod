package com.nexia.minigames.games.duels.map;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.utilities.world.StructureMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.notcoded.codelib.util.pos.EntityPos;
import net.notcoded.codelib.util.world.structure.Rotation;

import java.util.ArrayList;
import java.util.List;

public class DuelsMap {

    public static List<DuelsMap> duelsMaps = new ArrayList<>();

    public static List<String> stringDuelsMaps = new ArrayList<>();

    public String id;

    public boolean isAdventureSupported;

    public ItemStack item;
    
    public EntityPos p1Pos;
    
    public EntityPos p2Pos;

    public StructureMap structureMap;

    public static final DuelsMap CITY = new DuelsMap("city", true, new ItemStack(Items.SMOOTH_STONE), new EntityPos(-55, 80, 0, -90, 0), new EntityPos(17, 80, 0, 90, 0), new StructureMap(new Identifier("duels", "city"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-65, -11, -31), true));
    public static final DuelsMap NETHFLAT = new DuelsMap("nethflat", true, new ItemStack(Items.NETHERITE_BLOCK), new EntityPos(0, 80, -41, 0, 0), new EntityPos(0, 80, 41 ,180, 0), new StructureMap(new Identifier("duels", "nethflat"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-36, -3, -51), true));
    public static final DuelsMap PLAINS = new DuelsMap("plains", true, new ItemStack(Items.GRASS_BLOCK), new EntityPos(-71, 80, -16, 0, 0), new EntityPos(-71, 80, 34, 180, 0), new StructureMap(new Identifier("duels", "plains"), Rotation.CLOCKWISE_90, true, new BlockPos(0, 80, 0), new BlockPos(-40, -20, -31), true));
    public static final DuelsMap EDEN = new DuelsMap("eden", false, new ItemStack(Items.ALLIUM), new EntityPos(55, 80, 0, 90, 0), new EntityPos(-55, 80, 0, -90, 0), new StructureMap(new Identifier("duels", "eden"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-62, -7, -23), true));
    public static final DuelsMap CASTLE = new DuelsMap("castle", false, new ItemStack(Items.CRACKED_STONE_BRICKS), new EntityPos(0, 240, 25, -180, 0), new EntityPos(0, 240, -25, 0, 0), new StructureMap(new Identifier("duels", "castle"), Rotation.NO_ROTATION, true, new BlockPos(0, 238, 0), new BlockPos(-11, -4, -30), true));
    public static final DuelsMap BIGROOM = new DuelsMap("bigroom", false, new ItemStack(Items.OAK_PLANKS), new EntityPos(0, 80, 28, 180, 0), new EntityPos(0, 80, 28, 0, 0), new StructureMap(new Identifier("duels", "bigroom"), Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-62, -7, -23), true));

    public static DuelsMap identifyMap(String name) {
        for(DuelsMap map : DuelsMap.duelsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public DuelsMap(String id, boolean isAdventureSupported, ItemStack item, EntityPos p1Pos, EntityPos p2Pos, StructureMap structureMap) {
        this.id = id;
        this.isAdventureSupported = isAdventureSupported;
        this.item = item;
        
        this.p1Pos = p1Pos;
        this.p2Pos = p2Pos;
        this.structureMap = structureMap;

        DuelsMap.stringDuelsMaps.add(id);
        DuelsMap.duelsMaps.add(this);
    }
}