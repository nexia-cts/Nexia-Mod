package com.nexia.core.loader;

import com.nexia.core.listeners.*;

public class ListenerLoader {
    public static void registerListeners(){
        PlayerJoinListener.registerListener();
        PlayerLeaveListener.registerListener();
        PlayerRespawnListener.registerListener();
        PlayerBreakBlockListener.registerListener();
        PlayerDeathListener.registerListener();
        UseItemListener.registerListener();
    }
}
