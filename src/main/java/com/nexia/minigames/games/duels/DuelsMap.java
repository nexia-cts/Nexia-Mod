package com.nexia.minigames.games.duels;

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


    public static DuelsMap CITY = new DuelsMap("city", true, new ItemStack(Items.SMOOTH_STONE));

    public static DuelsMap NETHFLAT = new DuelsMap("nethflat", true, new ItemStack(Items.NETHERITE_BLOCK));

    public static DuelsMap PLAINS = new DuelsMap("plains", true, new ItemStack(Items.GRASS_BLOCK));

    public static DuelsMap EDEN = new DuelsMap("eden", false, new ItemStack(Items.ALLIUM));


    public static DuelsMap identifyMap(String name) {
        for(DuelsMap map : DuelsMap.duelsMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public DuelsMap(String id, boolean isAdventureSupported, ItemStack item) {
        this.id = id;
        this.isAdventureSupported = isAdventureSupported;
        this.item = item;

        DuelsMap.stringDuelsMaps.add(id);
        DuelsMap.duelsMaps.add(this);
    }
}