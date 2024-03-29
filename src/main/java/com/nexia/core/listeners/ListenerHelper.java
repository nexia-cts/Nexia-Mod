package com.nexia.core.listeners;

import com.nexia.core.listeners.metis.*;

public class ListenerHelper {
    public static void registerListeners() {
        // Factory Listeners
        PlayerJoinListener.registerListener();
        PlayerLeaveListener.registerListener();
        UseItemListener.registerListener();
        PlayerDropItemListener.registerListener();
        PlayerFoodLevelsChangeListener.registerListener();
        PlayerInteractBlockListener.registerListener();
        PlayerRespawnListener.registerListener();
    }
}
