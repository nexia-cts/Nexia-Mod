package com.nexia.core.utilities.player;

import com.nexia.base.player.PlayerData;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.FfaGameMode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.time.LocalDateTime;

public class CorePlayerData extends PlayerData {

    // Stuff not saved in files
    public PlayerGameMode gameMode;

    public FfaGameMode ffaGameMode;
    public ResourceKey<Level> spawnWorld;
    public EntityPos spawnPoint;

    public LocalDateTime combatTagEnd;

    public NexiaPlayer lastMessageSender;

    public ClientType clientType;

    public CorePlayerData(SavedPlayerData savedData) {
        super(savedData);

        this.combatTagEnd = LocalDateTime.now();

        this.gameMode = PlayerGameMode.LOBBY;
        this.ffaGameMode = null;
        this.spawnWorld = LobbyUtil.lobbyWorld.dimension();
        this.spawnPoint = LobbyUtil.lobbySpawn;

        this.lastMessageSender = null;

        this.clientType = null;
    }

    public enum ClientType {
        COMBAT_TEST,
        VIAFABRICPLUS,
        COMBATIFY
    }

}
