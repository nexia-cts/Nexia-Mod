package com.nexia.ffa;

import com.nexia.ffa.classic.config.ModConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {

	public static ModConfig classic;

	public static com.nexia.ffa.kits.config.ModConfig kits;

	public static com.nexia.ffa.sky.config.ModConfig sky;

	public static com.nexia.ffa.uhc.config.ModConfig uhc;

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		classic = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		AutoConfig.register(com.nexia.ffa.kits.config.ModConfig.class, GsonConfigSerializer::new);
		kits = AutoConfig.getConfigHolder(com.nexia.ffa.kits.config.ModConfig.class).getConfig();

		AutoConfig.register(com.nexia.ffa.sky.config.ModConfig.class, GsonConfigSerializer::new);
		sky = AutoConfig.getConfigHolder(com.nexia.ffa.sky.config.ModConfig.class).getConfig();

		AutoConfig.register(com.nexia.ffa.uhc.config.ModConfig.class, GsonConfigSerializer::new);
		uhc = AutoConfig.getConfigHolder(com.nexia.ffa.uhc.config.ModConfig.class).getConfig();
	}
}
