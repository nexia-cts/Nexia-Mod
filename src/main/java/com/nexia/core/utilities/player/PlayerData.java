package com.nexia.core.utilities.player;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.player.anticheat.Punishment;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.FfaGameMode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.time.LocalDateTime;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public PlayerGameMode gameMode;

    public FfaGameMode ffaGameMode;
    public ResourceKey<Level> spawnWorld;
    public EntityPos spawnPoint;

    public LocalDateTime combatTagEnd;

    public ServerPlayer lastMessageSender;

    public ClientType clientType;

    public Punishment punishment;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.combatTagEnd = LocalDateTime.now();

        this.gameMode = PlayerGameMode.LOBBY;
        this.ffaGameMode = null;
        this.spawnWorld = LobbyUtil.lobbyWorld.dimension();
        this.spawnPoint = LobbyUtil.lobbySpawn;

        this.lastMessageSender = null;

        this.clientType = null;

        this.punishment = new Punishment();
    }

    public enum ClientType {
        COMBAT_TEST,
        VIAFABRICPLUS,
        COMBATIFY
    }

}
