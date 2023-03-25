package com.nexia.core;

import com.nexia.core.config.ModConfig;
import com.nexia.core.loader.CommandLoader;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.minigames.games.bedwars.areas.BwDimension;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class Main implements ModInitializer {

	public static MinecraftServer server;

	public static ModConfig config;

	public static final String MOD_NAME = "Nexia";
	public static final String MOD_NAME_SHORT = "Nx";
	public static final String modConfigDir = NxFileUtil.makeDir(FabricLoader.getInstance().getConfigDir().toString() + "/nexia");
	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		CommandLoader.registerCommands();
		BwLoadShop.loadBedWarsShop(true);
		BwDimension.register();
	}
}
