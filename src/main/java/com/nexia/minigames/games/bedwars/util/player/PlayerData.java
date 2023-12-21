package com.nexia.minigames.games.bedwars.util.player;

import net.minecraft.server.level.ServerPlayer;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files

    public ServerPlayer combatTagPlayer;
    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;
        this.combatTagPlayer = null;
    }

}
