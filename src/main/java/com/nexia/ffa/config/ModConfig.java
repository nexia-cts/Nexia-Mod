package com.nexia.ffa.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "nexia-ffa")
public class ModConfig implements ConfigData {
    @Comment("The coordinates of the spawn")
    public double[] spawnCoordinates = {0.5, 80.0, 0.5};

    @Comment("The name of the ffa world.")
    public String worldName = "ffa:map";

}
