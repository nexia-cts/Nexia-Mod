package com.nexia.core.listeners;

import com.nexia.core.listeners.nexus.*;

public class ListenerHelper {
    public static void registerListeners() {
        // Nexus Listeners
        new PlayerJoinListener().registerListener();
        new PlayerDamageListener().registerListener();
        new PlayerDeathListener().registerListener();
        new PlayerHungerListener().registerListener();
        new PlayerLeaveListener().registerListener();
        new PlayerSwapHandItemsListener().registerListener();
        new PlayerUseItemListener().registerListener();
        new PlayerDropItemListener().registerListener();
        new PlayerRespawnListener().registerListener();
    }
}
