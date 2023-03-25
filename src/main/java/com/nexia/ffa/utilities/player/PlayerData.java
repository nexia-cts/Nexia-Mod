package com.nexia.ffa.utilities.player;

import net.minecraft.world.entity.player.Inventory;

public class PlayerData {
    public SavedPlayerData savedData;

    public Inventory FfaInventory;


    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.FfaInventory = null;
    }
}
