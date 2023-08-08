package com.nexia.core.utilities.player;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
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

    public ServerPlayer combatTagPlayer;
    public LocalDateTime combatTagEnd;

    public boolean isReportBanned;

    public ServerPlayer lastMessageSender;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.combatTagPlayer = null;
        this.combatTagEnd = LocalDateTime.now();

        this.isReportBanned = false;

        this.gameMode = PlayerGameMode.LOBBY;
        this.ffaGameMode = null;
        this.spawnWorld = LobbyUtil.lobbyWorld.dimension();
        this.spawnPoint = LobbyUtil.lobbySpawn;

        this.lastMessageSender = null;
    }

}
