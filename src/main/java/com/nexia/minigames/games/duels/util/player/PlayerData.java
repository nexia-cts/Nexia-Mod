package com.nexia.minigames.games.duels.util.player;

import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import net.minecraft.server.level.ServerPlayer;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files

    // Global
    public DuelGameMode gameMode;
    public boolean inDuel;
    public boolean inviting;
    public ServerPlayer invitingPlayer;
    public ServerPlayer spectatingPlayer;
    public boolean isDead;
    public String inviteMap;
    public String inviteKit;


    // Duels
    public DuelsGame duelsGame;
    public ServerPlayer duelPlayer;


    // Team Duels
    public TeamDuelsGame teamDuelsGame;
    public DuelsTeam duelsTeam;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.inDuel = false;
        this.inviting = false;
        this.gameMode = DuelGameMode.LOBBY;
        this.invitingPlayer = null;
        this.isDead = false;
        this.duelsTeam = null;
        this.duelPlayer = null;
        this.teamDuelsGame = null;
        this.spectatingPlayer = null;
        this.inviteMap = "";
        this.inviteKit = "";
        this.duelsGame = null;
    }

}
