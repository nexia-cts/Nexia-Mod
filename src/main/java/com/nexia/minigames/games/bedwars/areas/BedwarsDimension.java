package com.nexia.minigames.games.bedwars.areas;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class BedwarsDimension {

    public static final String DIMENSION_ID = "minigames";
    public static final String DIMENSION_NAME = "bedwars";

    public static final ResourceKey<Level> LEVEL_KEY = ResourceKey.create(
            Registry.DIMENSION_REGISTRY, new ResourceLocation(DIMENSION_ID, DIMENSION_NAME));

    private static final ResourceKey<DimensionType> DIMENSION_TYPE_KEY = ResourceKey.create(
            Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(DIMENSION_ID, "bedwars_type"));

    private static final ResourceKey<LevelStem> BEDWARS_WORLD_KEY = ResourceKey.create(
            Registry.LEVEL_STEM_REGISTRY, LEVEL_KEY.location());
    
    public static void register() {}

}
