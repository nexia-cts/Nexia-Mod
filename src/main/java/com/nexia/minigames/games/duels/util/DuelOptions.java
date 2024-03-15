package com.nexia.minigames.games.duels.util;

import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import net.notcoded.codelib.players.AccuratePlayer;

public class DuelOptions {

    public DuelsTeam duelsTeam;
    public AccuratePlayer spectatingPlayer;

    public static class InviteOptions {

        public boolean inviting;

        public AccuratePlayer invitingPlayer;

        public DuelsMap inviteMap;
        public String inviteKit;

        public boolean customDuel;

        public InviteOptions(AccuratePlayer invitingPlayer, boolean inviting, DuelsMap inviteMap, String inviteKit, boolean customDuel) {
            this.invitingPlayer = invitingPlayer;
            this.inviting = inviting;
            this.inviteMap = inviteMap;
            this.inviteKit = inviteKit;

            this.customDuel = customDuel;
        }

        public InviteOptions reset() {
            this.invitingPlayer = null;
            this.inviting = false;
            this.inviteMap = DuelsMap.CITY;
            this.inviteKit = "";
            this.customDuel = false;
            return this;
        }
    }

    public static class GameOptions {
        // Duels
        public DuelsGame duelsGame;
        public AccuratePlayer duelPlayer;

        // Custom
        public CustomDuelsGame customDuelsGame;
        public CustomTeamDuelsGame customTeamDuelsGame;

        // Team Duels
        public TeamDuelsGame teamDuelsGame;
        public DuelsTeam duelTeam;

        public GameOptions(DuelsGame duelsGame, AccuratePlayer duelPlayer) {
            this.duelsGame = duelsGame;
            this.duelPlayer = duelPlayer;
        }

        public GameOptions(CustomDuelsGame customDuelsGame, AccuratePlayer duelPlayer) {
            this.customDuelsGame = customDuelsGame;
            this.duelPlayer = duelPlayer;
        }

        public GameOptions(TeamDuelsGame teamDuelsGame, DuelsTeam duelTeam) {
            this.teamDuelsGame = teamDuelsGame;
            this.duelTeam = duelTeam;
        }

        public GameOptions(CustomTeamDuelsGame customTeamDuelsGame, DuelsTeam duelTeam) {
            this.customTeamDuelsGame = customTeamDuelsGame;
            this.duelTeam = duelTeam;
        }
    }

    public DuelOptions(AccuratePlayer spectatingPlayer, DuelsTeam duelsTeam) {
        this.spectatingPlayer = spectatingPlayer;
        this.duelsTeam = duelsTeam;
    }
}