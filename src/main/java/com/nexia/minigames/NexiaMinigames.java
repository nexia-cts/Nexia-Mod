package com.nexia.minigames;

import com.nexia.minigames.games.duels.DuelGameHandler;
import net.fabricmc.api.ModInitializer;

public class NexiaMinigames implements ModInitializer {
	@Override
	public void onInitialize() {
		DuelGameHandler.createDirectories();
	}
}
