package com.nexia.minigames.games.duels.team;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.event.player.PlayerDeathEvent;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class TeamDuelsGame extends DuelsGame {
    public DuelsTeam team1;

    public DuelsTeam team2;

    // Winner thingie
    public DuelsTeam winner = null;

    public DuelsTeam loser = null;

    public TeamDuelsGame(DuelsTeam team1, DuelsTeam team2, DuelGameMode gameMode, DuelsMap map, ServerLevel level, int endTime, int startTime) {
        super(null, null, gameMode, map, level, endTime, startTime);
        this.team1 = team1;
        this.team2 = team2;
    }

    public String detectBrokenGame() {
        if (this.team1 == null)
            return "Team 1 is not set [NULL]";
        if (this.team1.getLeader() == null || this.team1.getLeader().unwrap() == null)
            return "Team 1 Leader is not set [NULL]";

        if (this.team2 == null)
            return "Team 2 is not set [NULL]";
        if (this.team2.getLeader() == null || this.team2.getLeader().unwrap() == null)
            return "Team 2 Leader is not set [NULL]";

        if (this.isEnding) {
            if (this.winner == null)
                return "Winner Team is not set [NULL]";
            if (this.winner.getLeader() == null || this.winner.getLeader().unwrap() == null)
                return "Winner Team Leader is not set [NULL]";

            if (this.loser == null)
                return "Loser Team is not set [NULL]";
            if (this.loser.getLeader() == null || this.loser.getLeader().unwrap() == null)
                return "Loser Team Leader is not set [NULL]";
        }

        return null;
    }

    public static TeamDuelsGame startGame(@NotNull DuelsTeam team1, @NotNull DuelsTeam team2, String stringGameMode, @Nullable DuelsMap selectedMap) {
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            gameMode = DuelGameMode.CLASSIC;
            NexiaCore.logger.error(String.format("[Nexia]: Invalid duel gamemode (%s) selected! Using fallback one.", stringGameMode));
            stringGameMode = "CLASSIC";
        }

        team1.alive.clear();
        team1.alive.addAll(team1.all);

        team2.alive.clear();
        team2.alive.addAll(team2.all);

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), gameMode.hasRegen);
        if (selectedMap == null) {
            selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
        }

        selectedMap.structureMap.pasteMap(duelLevel);

        TeamDuelsGame game = new TeamDuelsGame(team1, team2, gameMode, selectedMap, duelLevel, 5, 5);
        DuelGameHandler.teamDuelsGames.add(game);

        for (NexiaPlayer player : team1.all) {
            DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

            data.gameMode = gameMode;
            data.gameOptions = new DuelOptions.GameOptions(game, team2);
            data.inviteOptions.reset();
            data.duelOptions.spectatingPlayer = null;
            data.inDuel = true;

            removeQueue(player, null, true);

            player.setGameMode(Minecraft.GameMode.ADVENTURE);

            player.sendNexiaMessage(Component.text("Your opponent: ", ChatFormat.normalColor)
                    .append(Component.text(team2.getLeader().getRawName() + "'s Team", ChatFormat.brandColor2))
            );

            player.reset(true, Minecraft.GameMode.ADVENTURE);
            DuelGameHandler.loadInventory(player, stringGameMode);

            if (!gameMode.hasSaturation) {
                player.addTag(LobbyUtil.NO_SATURATION_TAG);
            }

            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
        }

        for (NexiaPlayer player : team2.all) {
            DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

            data.gameMode = gameMode;
            data.gameOptions = new DuelOptions.GameOptions(game, team1);
            data.inviteOptions.reset();
            data.duelOptions.spectatingPlayer = null;
            data.inDuel = true;

            removeQueue(player, null, true);

            selectedMap.p2Pos.teleportPlayer(duelLevel, player.unwrap());

            player.sendNexiaMessage(
                    Component.text("Your opponent: ", ChatFormat.normalColor)
                            .append(Component.text(team1.getLeader().getRawName() + "'s Team", ChatFormat.brandColor2))
            );

            player.reset(true, Minecraft.GameMode.ADVENTURE);
            DuelGameHandler.loadInventory(player, stringGameMode);

            if (!gameMode.hasSaturation) {
                player.addTag(LobbyUtil.NO_SATURATION_TAG);
            }

            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
        }

        game.uuid = gameUUID;

        return game;
    }

    public void duelSecond() {
        String isBroken = this.detectBrokenGame();
        if (isBroken != null && !this.isEnding) {
            Component error = ChatFormat.nexiaMessage
                    .append(Component.text("The game you were in was identified as broken, please contact a developer with a video of the last 30 seconds.", ChatFormat.normalColor));

            Component errormsg = Component.text("Cause: " + isBroken);
            for (ServerPlayer player : this.level.players()) {
                NexiaPlayer nexusPlayer = new NexiaPlayer(player);
                nexusPlayer.sendMessage(error);
                nexusPlayer.sendMessage(errormsg);
            }

            this.hasStarted = true;
            this.isEnding = true;
            this.shouldWait = false;

            DuelsTeam notNullTeam = this.team1;

            if(notNullTeam != null) notNullTeam = this.team2;
            if(notNullTeam != null) this.endGame(notNullTeam, null, false);
        }
        if (this.isEnding) {

            if(this.winner != null && this.winner.alive != null && !this.winner.alive.isEmpty() && this.shouldWait) {
                int color = 160 * 65536 + 248;

                // r * 65536 + g * 256 + b;
                DuelGameHandler.winnerRockets(this.winner.alive.get(new Random().nextInt(this.winner.alive.size())), this.level, color);
            }

            this.currentEndTime++;

            if (this.currentEndTime >= this.endTime || !this.shouldWait) {
                DuelsTeam winnerTeam = this.winner == null ? this.team1 : this.winner;
                DuelsTeam loserTeam = this.loser == null ? this.team2 : this.loser;

                for(ServerPlayer serverPlayer : this.level.players()) {
                    NexiaPlayer player = new NexiaPlayer(serverPlayer);

                    ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).gameOptions = null;
                    player.runCommand("/hub", 0, false);
                }

                if(loserTeam != null) loserTeam.refreshTeam();
                if(winnerTeam != null) winnerTeam.refreshTeam();


                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));
                removeDuelsGame();
                this.isEnding = false;
                return;
            }
        }
        if (!this.hasStarted) {

            this.currentStartTime--;

            for (NexiaPlayer player : this.team1.alive) {
                this.map.p1Pos.teleportPlayer(this.level, player.unwrap());
            }
            for (NexiaPlayer player : this.team2.alive) {
                this.map.p2Pos.teleportPlayer(this.level, player.unwrap());
            }

            if (this.startTime - this.currentStartTime >= this.startTime) {

                for (NexiaPlayer player : this.team1.alive) {
                    player.sendSound(new EntityPos(player.unwrap()), SoundEvents.RAID_HORN, SoundSource.AMBIENT,
                            10, 1);
                    player.setGameMode(this.gameMode != null ? this.gameMode.gameMode : Minecraft.GameMode.SURVIVAL);
                    player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                for (NexiaPlayer player : this.team2.alive) {
                    player.sendSound(new EntityPos(player.unwrap()), SoundEvents.RAID_HORN, SoundSource.AMBIENT,
                            10, 1);
                    player.setGameMode(this.gameMode != null ? this.gameMode.gameMode : Minecraft.GameMode.SURVIVAL);
                    player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                this.hasStarted = true;
                return;
            }

            Title title = DuelGameHandler.getTitle(this.currentStartTime);

            for (NexiaPlayer player : this.team1.alive) {
                player.sendTitle(title);
                player.sendSound(new EntityPos(player.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10,
                        1);
            }
            for (NexiaPlayer player : this.team2.alive) {
                player.sendTitle(title);
                player.sendSound(new EntityPos(player.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10,
                        1);
            }
        }
    }

    public void endGame(@NotNull DuelsTeam loserTeam, @Nullable DuelsTeam winnerTeam, boolean wait) {
        this.winner = winnerTeam;
        this.loser = loserTeam;
        if (winnerTeam == null) {
            this.winner = this.team1;
            if (loserTeam == this.team1) {
                this.winner = this.team2;
            }
        }

        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;

        Component win = Component.text("The game was a ")
                .color(ChatFormat.normalColor)
                .append(Component.text("draw", ChatFormat.brandColor2))
                .append(Component.text("!", ChatFormat.normalColor));

        Component titleLose = Component.text("Draw")
                .color(ChatFormat.brandColor2);
        Component subtitleLose;

        Component titleWin = titleLose;
        Component subtitleWin = win;

        if ((winnerTeam == null || winnerTeam.getLeader() == null || winnerTeam.getLeader().unwrap() == null)) {
            for (ServerPlayer serverPlayer : level.players()) {
                NexiaPlayer player = new NexiaPlayer(serverPlayer);
                player.sendTitle(Title.title(titleWin, subtitleWin));
                player.sendMessage(win);
            }
            return;
        }

        win = Component.text(winnerTeam.getLeader().getRawName() + "'s Team", ChatFormat.brandColor2)
                .append(Component.text(" has won the duel!", ChatFormat.normalColor));

        titleLose = Component.text("You lost!", ChatFormat.brandColor2);
        subtitleLose = Component.text("You have lost against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(winnerTeam.getLeader().getRawName() + "'s Team")
                        .color(ChatFormat.brandColor2));

        titleWin = Component.text("You won!", ChatFormat.brandColor2);
        subtitleWin = Component.text("You have won against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(loserTeam.getLeader().getRawName() + "'s Team")
                        .color(ChatFormat.brandColor2));

        for (NexiaPlayer player : loserTeam.all) {
            PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player).savedData.incrementInteger("losses");
            player.sendTitle(Title.title(titleLose, subtitleLose));
            player.sendMessage(win);
        }

        for (NexiaPlayer player : winnerTeam.all) {
            PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player).savedData.incrementInteger("wins");
            player.sendTitle(Title.title(titleWin, subtitleWin));
            player.sendMessage(win);
        }
    }

    public void death(@NotNull NexiaPlayer victim, PlayerDeathEvent source) {
        DuelsPlayerData victimData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(victim);
        DuelsTeam victimTeam = victimData.duelOptions.duelsTeam;

        if (victimTeam == null || this.isEnding) return;

        victim.unwrap().destroyVanishingCursedItems();
        victim.unwrap().inventory.dropAll();
        victimTeam.alive.remove(victim);

        boolean isVictimTeamDead = victimTeam.alive.isEmpty();

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim.unwrap());

        if (attacker != null) {
            DuelsPlayerData attackerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(attacker.getUUID());
            if (getDuelsGame(attackerData.gameOptions) != null && getDuelsGame(attackerData.gameOptions).equals(this) && isVictimTeamDead) {
                this.endGame(victimTeam, attackerData.duelOptions.duelsTeam, true);
            }
            return;
        }
        if (isVictimTeamDead) {
            if (this.team1 == victimTeam)
                this.endGame(victimTeam, this.team2, true);
            else if (this.team2 == victimTeam)
                this.endGame(victimTeam, this.team1, true);
        }
    }

    @Override
    public DuelsGame getDuelsGame(DuelOptions.GameOptions gameOptions) {
        return gameOptions.teamDuelsGame;
    }

    @Override
    public void removeDuelsGame() {
        DuelGameHandler.teamDuelsGames.remove(this);
    }
}