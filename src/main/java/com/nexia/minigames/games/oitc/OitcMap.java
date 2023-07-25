package com.nexia.minigames.games.oitc;

import java.util.ArrayList;
import java.util.List;

public class OitcMap {

    public static List<OitcMap> oitcMaps = new ArrayList<>();

    public static List<String> stringOitcMaps = new ArrayList<>();

    public String id;

    public static OitcMap CITY = new OitcMap("city");


    public static OitcMap identifyMap(String name) {
        for(OitcMap map : OitcMap.oitcMaps) {
            if(map.id.equalsIgnoreCase(name)) return map;
        }
        return null;
    }

    public OitcMap(String id) {
        this.id = id;

        OitcMap.stringOitcMaps.add(id);
        OitcMap.oitcMaps.add(this);
    }
}