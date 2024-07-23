package com.nexia.minigames.games.duels.util.player;

import com.nexia.base.player.PlayerData;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.util.DuelOptions;

public class DuelsPlayerData extends PlayerData {
    // Stuff not saved in files

    // Global
    public DuelGameMode gameMode;
    public boolean inDuel;
    public KitRoom kitRoom;

    // Kit Editor
    public String editingKit;

    // Kit Layout
    public String editingLayout;

    // Duels
    public DuelOptions duelOptions;
    public DuelOptions.GameOptions gameOptions;
    public DuelOptions.InviteOptions inviteOptions;

    public DuelsPlayerData(DuelsSavedPlayerData savedData) {
        super(savedData);

        this.gameMode = DuelGameMode.LOBBY;
        this.inDuel = false;

        this.duelOptions = new DuelOptions(null, null);
        this.inviteOptions = new DuelOptions.InviteOptions(null, false, DuelsMap.CITY, "null", false);
        this.gameOptions = null;

        this.kitRoom = null;
        this.editingKit = "";
        this.editingLayout = "";

        /*
        this.invitingPlayer = null;
        this.isDead = false;
        this.duelsTeam = null;
        this.duelPlayer = null;
        this.teamDuelsGame = null;
        this.spectatingPlayer = null;
        this.inviteMap = DuelsMap.CITY;
        this.inviteKit = "";
        this.duelsGame = null;

         */
    }

}
