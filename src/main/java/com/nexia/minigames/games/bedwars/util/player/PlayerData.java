package com.nexia.minigames.games.bedwars.util.player;

import net.minecraft.server.level.ServerPlayer;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    public ServerPlayer combatTagPlayer;

    // Stuff not saved in files
    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;
        this.combatTagPlayer = null;
    }

}
