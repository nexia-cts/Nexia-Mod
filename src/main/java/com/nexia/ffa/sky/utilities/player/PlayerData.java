package com.nexia.ffa.sky.utilities.player;

import net.minecraft.world.entity.player.Inventory;

public class PlayerData {
    public SavedPlayerData savedData;


    public Inventory ffaInventory;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;
        this.ffaInventory = null;
    }
}
