package com.nexia.ffa.uhc.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "nexia-ffa-uhc")
public class ModConfig implements ConfigData {
    @Comment("The coordinates of the spawn")
    public double[] spawnCoordinates = {0.5, 128.0, -7.5};

    @Comment("The coordinates of the spawn")
    public double[] mapCenterCoordinates = {0.5, 128.0, 0.5};

    @Comment("The name of the ffa world.")
    public String worldName = "ffa:uhc";

}
