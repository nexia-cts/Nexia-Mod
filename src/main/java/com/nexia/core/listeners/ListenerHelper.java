package com.nexia.core.listeners;

import com.nexia.core.listeners.factory.*;

public class ListenerHelper {
    public static void registerListeners() {
        // Factory Listeners
        new PlayerJoinListener().registerListener();
        new PlayerLeaveListener().registerListener();
        new UseItemListener().registerListener();
        new PlayerDropItemListener().registerListener();
        new PlayerRespawnListener().registerListener();
    }
}
