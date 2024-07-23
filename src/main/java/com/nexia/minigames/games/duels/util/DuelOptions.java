package com.nexia.minigames.games.duels.util;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;

public class DuelOptions {

    public DuelsTeam duelsTeam;
    public NexiaPlayer spectatingPlayer;

    public static class InviteOptions {

        public boolean inviting;

        public NexiaPlayer invitingPlayer;

        public DuelsMap inviteMap;

        public String inviteKit;
        public String inviteKit2;

        public boolean customDuel;

        public boolean perCustomDuel;

        public InviteOptions(NexiaPlayer invitingPlayer, boolean inviting, DuelsMap inviteMap, String inviteKit, boolean customDuel) {
            this.invitingPlayer = invitingPlayer;
            this.inviting = inviting;
            this.inviteMap = inviteMap;
            this.inviteKit = inviteKit;
            this.inviteKit2 = null;

            this.customDuel = customDuel;
            this.perCustomDuel = false;
        }

        public InviteOptions(NexiaPlayer invitingPlayer, boolean inviting, DuelsMap inviteMap, String inviteKit, String inviteKit2, boolean customDuel) {
            this.invitingPlayer = invitingPlayer;
            this.inviting = inviting;
            this.inviteMap = inviteMap;
            this.inviteKit = inviteKit;
            this.inviteKit2 = inviteKit2;

            this.customDuel = customDuel;
            this.perCustomDuel = customDuel && (inviteKit2 != null && !inviteKit2.trim().isEmpty());
        }

        public InviteOptions reset() {
            this.invitingPlayer = null;
            this.inviting = false;
            this.inviteMap = DuelsMap.CITY;
            this.inviteKit = "";
            this.inviteKit2 = null;
            this.customDuel = false;
            this.perCustomDuel = false;
            return this;
        }
    }

    public static class GameOptions {
        // Duels
        public DuelsGame duelsGame;
        public NexiaPlayer duelPlayer;

        // Custom
        public CustomDuelsGame customDuelsGame;
        public CustomTeamDuelsGame customTeamDuelsGame;

        // Team Duels
        public TeamDuelsGame teamDuelsGame;
        public DuelsTeam duelTeam;

        public GameOptions(DuelsGame duelsGame, NexiaPlayer duelPlayer) {
            this.duelsGame = duelsGame;
            this.duelPlayer = duelPlayer;
        }

        public GameOptions(CustomDuelsGame customDuelsGame, NexiaPlayer duelPlayer) {
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

    public DuelOptions(NexiaPlayer spectatingPlayer, DuelsTeam duelsTeam) {
        this.spectatingPlayer = spectatingPlayer;
        this.duelsTeam = duelsTeam;
    }
}
