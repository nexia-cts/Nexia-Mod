package com.nexia.core;

import com.combatreforged.metis.api.MetisAPI;
import com.combatreforged.metis.api.MetisServer;
import com.combatreforged.metis.api.entrypoint.MetisPlugin;
import com.nexia.core.config.ModConfig;
import com.nexia.core.listeners.*;
import com.nexia.core.loader.CommandLoader;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.time.ServerTime;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer, MetisPlugin {

	public static MinecraftServer server;

	public static ModConfig config;

	public static final String MOD_NAME = "Nexia";
	public static final String MOD_NAME_SHORT = "Nx";
	public static Logger logger = LogManager.getLogger(MOD_NAME);
	public static final String modConfigDir = NxFileUtil.makeDir(FabricLoader.getInstance().getConfigDir().toString() + "/nexia");

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		logger.info("Loading mod...");
		logger.info("Registering commands....");
		CommandLoader.registerCommands();
		logger.info("Registered commands.");
	}

	@Override
	@SuppressWarnings("FutureReturnValueIgnored")
	public void onMetisLoad(MetisAPI api, MetisServer server) {
		Main.logger.info("Loading metis version...");

		ServerTime.metisServer = server;
		ServerTime.metisAPI = api;
		ServerTime.scheduler = api.getScheduler();

		Main.logger.info("Registering listeners....");
		ListenerHelper.registerListeners();
		Main.logger.info("Registered listeners.");
		Main.logger.info("Registering metis commands....");
		CommandLoader.registerMetisCommands();
		Main.logger.info("Registered commands.");
	}
}
