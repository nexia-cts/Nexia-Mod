package com.nexia.minigames.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

import java.util.ArrayList;

@Config(name = "nexia-minigames")
public class ModConfig implements ConfigData {
    public ArrayList<String> duelsMaps = new ArrayList<>();
}
