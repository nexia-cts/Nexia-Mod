package com.nexia.core.listeners;

public class ListenerHelper {
    public static void registerListeners() {
        PlayerJoinListener.registerListener();
        PlayerLeaveListener.registerListener();
        PlayerRespawnListener.registerListener();
        PlayerBreakBlockListener.registerListener();
        UseItemListener.registerListener();
        PlayerDropItemListener.registerListener();
    }
}
