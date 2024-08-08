package com.nexia.core.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

@Config(name = "nexia-core")
public class ModConfig implements ConfigData {

    public String[] rules = {""};

    public String serverType = "";

    public boolean debugMode = false;
}
