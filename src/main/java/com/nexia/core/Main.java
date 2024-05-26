package com.nexia.core;

import com.combatreforged.metis.api.MetisAPI;
import com.combatreforged.metis.api.MetisServer;
import com.combatreforged.metis.api.entrypoint.MetisPlugin;
import com.nexia.core.config.ModConfig;
import com.nexia.core.listeners.ListenerHelper;
import com.nexia.core.loader.CommandLoader;
import com.nexia.core.networking.NetworkingHandler;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.anticheat.Punishment;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.gatoanticheat.events.PlayerDetectionEvent;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer, MetisPlugin {
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

		PlayerDetectionEvent.REACH.register((player, entity) -> {
			Punishment punishment = PlayerDataManager.get(player).punishment;
			punishment.reachOffences++;
			punishment.check((ServerPlayer) player);
		});

		PlayerDetectionEvent.NO_SWING.register((player, entity) -> {
			Punishment punishment = PlayerDataManager.get(player).punishment;
			punishment.noSwingOffences++;
			punishment.check((ServerPlayer) player);
		});

		networkingHandler = new NetworkingHandler();
	}

	@Override
	@SuppressWarnings("FutureReturnValueIgnored")
	public void onMetisLoad(MetisAPI api, MetisServer server) {
		Main.logger.info("Loading Metis API...");

		ServerTime.metisServer = server;
		ServerTime.metisAPI = api;
		ServerTime.scheduler = api.getScheduler();

		Main.logger.info("Registering listeners...");
		ListenerHelper.registerListeners();
		Main.logger.info("Registered listeners.");
		Main.logger.info("Registering metis commands...");
		CommandLoader.registerMetisCommands();
		Main.logger.info("Registered metis commands.");
	}
}
