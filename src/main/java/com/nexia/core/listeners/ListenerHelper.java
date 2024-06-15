package com.nexia.core.listeners;

import com.nexia.core.listeners.nexus.*;

public class ListenerHelper {
    public static void registerListeners() {
        // Nexus Listeners
        PlayerJoinListener.registerListener();
        PlayerLeaveListener.registerListener();
        UseItemListener.registerListener();
        PlayerDropItemListener.registerListener();
        PlayerRespawnListener.registerListener();
    }
}
