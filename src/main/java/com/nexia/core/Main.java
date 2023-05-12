package com.nexia.core;

import com.combatreforged.factory.api.FactoryAPI;
import com.combatreforged.factory.api.FactoryServer;
import com.combatreforged.factory.api.entrypoint.FactoryPlugin;
import com.combatreforged.factory.api.event.entity.LivingEntityDeathEvent;
import com.combatreforged.factory.api.event.player.PlayerDeathEvent;
import com.combatreforged.factory.api.event.player.PlayerRespawnEvent;
import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.config.ModConfig;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.listeners.*;
import com.nexia.core.loader.CommandLoader;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.DuelsGame;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

public class Main implements ModInitializer, FactoryPlugin {

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
	public void onFactoryLoad(FactoryAPI api, FactoryServer server) {
		Main.logger.info("Loading factory version...");

		ServerTime.factoryServer = server;
		ServerTime.factoryAPI = api;
		ServerTime.scheduler = api.getScheduler();

		Main.logger.info("Registering listeners....");
		ListenerHelper.registerListeners();
		Main.logger.info("Registered listeners.");
	}
}
