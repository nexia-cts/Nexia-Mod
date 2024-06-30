package com.nexia.core;

import com.nexia.nexus.api.NexusAPI;
import com.nexia.nexus.api.NexusServer;
import com.nexia.nexus.api.entrypoint.NexusPlugin;
import com.nexia.core.config.ModConfig;
import com.nexia.core.listeners.ListenerHelper;
import com.nexia.core.loader.CommandLoader;
import com.nexia.core.networking.NetworkingHandler;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.time.ServerTime;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer, NexusPlugin {
	public static ModConfig config;

	public static final String MOD_NAME = "Nexia";
	public static Logger logger = LogManager.getLogger(MOD_NAME);
	public static final String modConfigDir = NxFileUtil.makeDir(FabricLoader.getInstance().getConfigDir().toString() + "/nexia");

	public NetworkingHandler networkingHandler;

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		logger.info("Loading mod...");
		logger.info("Registering commands...");
		CommandLoader.registerCommands();
		logger.info("Registered commands.");

		networkingHandler = new NetworkingHandler();
	}

	@Override
	@SuppressWarnings("FutureReturnValueIgnored")
	public void onNexusLoad(NexusAPI api, NexusServer server) {
		Main.logger.info("Loading Nexus API...");

		ServerTime.nexusServer = server;
		ServerTime.nexusAPI = api;
		ServerTime.scheduler = api.getScheduler();

		Main.logger.info("Registering listeners...");
		ListenerHelper.registerListeners();
		Main.logger.info("Registered listeners.");
		Main.logger.info("Registering Nexus commands...");
		CommandLoader.registerNexusCommands();
		Main.logger.info("Registered Nexus commands.");
	}
}
