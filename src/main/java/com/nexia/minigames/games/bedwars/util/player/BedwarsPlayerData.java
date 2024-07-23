package com.nexia.minigames.games.bedwars.util.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import net.minecraft.server.level.ServerPlayer;

public class BedwarsPlayerData extends PlayerData {
    public ServerPlayer combatTagPlayer;

    // Stuff not saved in files
    public BedwarsPlayerData(SavedPlayerData savedData) {
        super(savedData);
        this.combatTagPlayer = null;
    }

}
