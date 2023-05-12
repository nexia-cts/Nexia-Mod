package com.nexia.core.listeners;

public class ListenerHelper {
    public static void registerListeners() {
        PlayerJoinListener.registerListener();
        PlayerLeaveListener.registerListener();
        UseItemListener.registerListener();
        PlayerDropItemListener.registerListener();
    }
}
