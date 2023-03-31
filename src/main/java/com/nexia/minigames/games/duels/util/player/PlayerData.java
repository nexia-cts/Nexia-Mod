package com.nexia.minigames.games.duels.util.player;

import com.nexia.minigames.games.duels.DuelGameMode;
import net.minecraft.server.level.ServerPlayer;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public DuelGameMode gameMode;

    public boolean inDuel;

    public boolean inviting;

    public ServerPlayer invitingPlayer;

    public boolean isDead;


    public String inviteMap;

    public ServerPlayer duelPlayer;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.inDuel = false;
        this.inviting = false;
        this.gameMode = DuelGameMode.LOBBY;
        this.invitingPlayer = null;
        this.isDead = false;
        this.duelPlayer = null;
        this.inviteMap = "";
    }

}
