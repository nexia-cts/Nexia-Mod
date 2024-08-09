package com.nexia.core;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.utilities.database.MongoManager;
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
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NexiaCore implements ModInitializer, NexusPlugin {
    public static ModConfig config;

	public static final String MOD_NAME = "Nexia";
	public static Logger logger = LogManager.getLogger(MOD_NAME);
	public static final String modConfigDir = NxFileUtil.makeDir(FabricLoader.getInstance().getConfigDir().toString() + "/nexia");

	public NetworkingHandler networkingHandler;
	public static MongoManager mongoManager;

	public static final ResourceLocation CONVENTIONAL_BRIDGING_UPDATE_ID = new ResourceLocation("c", "update_status");

	public static final ResourceLocation CORE_DATA_MANAGER = id("core");

	public static final ResourceLocation DISCORD_DATA_MANAGER = id("discord");

	public static final ResourceLocation FFA_CLASSIC_DATA_MANAGER = id("ffa_classic");
	public static final ResourceLocation FFA_KITS_DATA_MANAGER = id("ffa_kits");
	public static final ResourceLocation FFA_POT_DATA_MANAGER = id("ffa_pot");
	public static final ResourceLocation FFA_SKY_DATA_MANAGER = id("ffa_sky");
	public static final ResourceLocation FFA_UHC_DATA_MANAGER = id("ffa_uhc");

	public static final ResourceLocation BEDWARS_DATA_MANAGER = id("bedwars");

	public static final ResourceLocation DUELS_DATA_MANAGER = id("duels");

	public static final ResourceLocation FOOTBALL_DATA_MANAGER = id("football");

	public static final ResourceLocation OITC_DATA_MANAGER = id("oitc");

	public static final ResourceLocation SKYWARS_DATA_MANAGER = id("skywars");

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		logger.info("Loading mod...");
		PlayerDataManager.init();
		logger.info("Registering commands...");
		CommandLoader.registerCommands();
		logger.info("Registered commands.");

		networkingHandler = new NetworkingHandler();
		mongoManager = new MongoManager();
	}

	@Override
	@SuppressWarnings("FutureReturnValueIgnored")
	public void onNexusLoad(NexusAPI api, NexusServer server) {
		logger.info("Loading Nexus API...");

		ServerTime.nexusServer = server;
		ServerTime.nexusAPI = api;
		ServerTime.scheduler = api.getScheduler();

		logger.info("Registering listeners...");
		ListenerHelper.registerListeners();
		logger.info("Registered listeners.");
		logger.info("Registering Nexus commands...");
		CommandLoader.registerNexusCommands();
		logger.info("Registered Nexus commands.");
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation("nexia", path);
	}
}
