package com.nexia.minigames.games.duels.util;

import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import net.minecraft.server.level.ServerPlayer;

public class DuelOptions {

    public DuelsTeam duelsTeam;
    public ServerPlayer spectatingPlayer;

    public static class InviteOptions {

        public boolean inviting;

        public ServerPlayer invitingPlayer;

        public DuelsMap inviteMap;

        public String inviteKit;
        public String inviteKit2;

        public boolean customDuel;

        public boolean perCustomDuel;

        public InviteOptions(ServerPlayer invitingPlayer, boolean inviting, DuelsMap inviteMap, String inviteKit, boolean customDuel) {
            this.invitingPlayer = invitingPlayer;
            this.inviting = inviting;
            this.inviteMap = inviteMap;
            this.inviteKit = inviteKit;
            this.inviteKit2 = null;

            this.customDuel = customDuel;
            this.perCustomDuel = false;
        }

        public InviteOptions(ServerPlayer invitingPlayer, boolean inviting, DuelsMap inviteMap, String inviteKit, String inviteKit2, boolean customDuel) {
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
        public ServerPlayer duelPlayer;

        // Custom
        public CustomDuelsGame customDuelsGame;
        public CustomTeamDuelsGame customTeamDuelsGame;

        // Team Duels
        public TeamDuelsGame teamDuelsGame;
        public DuelsTeam duelTeam;

        public GameOptions(DuelsGame duelsGame, ServerPlayer duelPlayer) {
            this.duelsGame = duelsGame;
            this.duelPlayer = duelPlayer;
        }

        public GameOptions(CustomDuelsGame customDuelsGame, ServerPlayer duelPlayer) {
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

    public DuelOptions(ServerPlayer spectatingPlayer, DuelsTeam duelsTeam) {
        this.spectatingPlayer = spectatingPlayer;
        this.duelsTeam = duelsTeam;
    }
}
