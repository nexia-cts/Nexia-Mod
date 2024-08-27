package com.nexia.minigames.games.duels.map;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.nexus.api.util.Identifier;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    public static final DuelsMap COURTYARD = new DuelsMap("Courtyard", true, Items.LODESTONE, new EntityPos(0.5, 80, 24.5, 180, 0), new EntityPos(0.5, 80, -23.5, 0, 0), new StructureMap(new Identifier("duels", "courtyard"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-70, -10, -70), true));
    public static final DuelsMap COLOSSEUM = new DuelsMap("Colosseum", true, Items.SANDSTONE, new EntityPos(0.5, 80, 24.5, 180, 0), new EntityPos(0.5, 80, -23.5, 0, 0), new StructureMap(new Identifier("duels", "colosseum"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-55, -12, -61), true));
    public static final DuelsMap PLAINS = new DuelsMap("Plains", true, Items.GRASS_BLOCK, new EntityPos(-63.5, 80, -0.5, -90, 0), new EntityPos(64.5, 80, 0.5, 90, 0), new StructureMap(new Identifier("duels", "plains"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-78, -8, -59), true));
    public static final DuelsMap NETHFLAT = new DuelsMap("Neth Flat", true, Items.NETHERITE_BLOCK, new EntityPos(0, 80, -41, 0, 0), new EntityPos(0, 80, 41 ,180, 0), new StructureMap(new Identifier("duels", "nethflat"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-36, -3, -51), true));
    public static final DuelsMap EDEN = new DuelsMap("Eden", false, Items.ALLIUM, new EntityPos(55, 80, 0, 90, 0), new EntityPos(-55, 80, 0, -90, 0), new StructureMap(new Identifier("duels", "eden"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-62, -7, -23), true));
    public static final DuelsMap CASTLE = new DuelsMap("Castle", false, Items.CRACKED_STONE_BRICKS, new EntityPos(0, 240, 25, -180, 0), new EntityPos(0, 240, -25, 0, 0), new StructureMap(new Identifier("duels", "castle"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 238, 0), new BlockPos(-11, -4, -30), true));
    public static final DuelsMap BIGROOM = new DuelsMap("Big Room", true, Items.OAK_PLANKS, new EntityPos(-40.5, 80, 0.5, -90, 0), new EntityPos(41.5, 80, 0.5, 90, 0), new StructureMap(new Identifier("duels", "bigroom"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-50, -16, -65), true));
    public static final DuelsMap CITY = new DuelsMap("city", true, Items.SMOOTH_STONE, new EntityPos(-55, 80, 0, -90, 0), new EntityPos(17, 80, 0, 90, 0), new StructureMap(new Identifier("duels", "city"), StructureMap.Rotation.NO_ROTATION, true, new BlockPos(0, 80, 0), new BlockPos(-65, -11, -31), true));
    public static DuelsMap identifyMap(String name) {
        for(DuelsMap map : DuelsMap.duelsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public DuelsMap(String id, boolean isAdventureSupported, Item item, EntityPos p1Pos, EntityPos p2Pos, StructureMap structureMap) {
        this.id = id;
        this.isAdventureSupported = isAdventureSupported;
        
        ItemStack itemStack = new ItemStack(item);
        itemStack.setHoverName(ObjectMappings.convertComponent(Component.text(id, ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false)));
        itemStack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        itemStack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        
        this.item = itemStack;
        
        this.p1Pos = p1Pos;
        this.p2Pos = p2Pos;
        this.structureMap = structureMap;

        DuelsMap.stringDuelsMaps.add(id);
        DuelsMap.duelsMaps.add(this);
    }
}