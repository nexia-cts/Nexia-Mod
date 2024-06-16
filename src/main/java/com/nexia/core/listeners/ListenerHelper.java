package com.nexia.core.listeners;

import com.nexia.core.listeners.nexus.*;

public class ListenerHelper {
    public static void registerListeners() {
        // Nexus Listeners
        new PlayerJoinListener().registerListener();
        new PlayerLeaveListener().registerListener();
        new PlayerSwapHandItemsListener().registerListener();
        new UseItemListener().registerListener();
        new PlayerDropItemListener().registerListener();
        new PlayerRespawnListener().registerListener();
    }
}
