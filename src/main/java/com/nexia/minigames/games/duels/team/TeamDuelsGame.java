package com.nexia.minigames.games.duels.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import net.notcoded.codelib.players.AccuratePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class TeamDuelsGame { // implements Runnable{
    public DuelsTeam team1;

    public DuelsTeam team2;

    public UUID uuid;

    public DuelGameMode gameMode;

    public DuelsMap map;

    public boolean isEnding = false;

    public boolean hasStarted = false;

    public int startTime;

    private int currentStartTime = 5;

    public int endTime;

    private int currentEndTime = 0;

    public ServerLevel level;

    public ArrayList<AccuratePlayer> spectators = new ArrayList<>();

    // Winner thingie
    public DuelsTeam winner = null;

    public DuelsTeam loser = null;

    private boolean shouldWait = false;

    public TeamDuelsGame(DuelsTeam team1, DuelsTeam team2, DuelGameMode gameMode, DuelsMap map, ServerLevel level, int endTime, int startTime) {
        this.team1 = team1;
        this.team2 = team2;
        this.gameMode = gameMode;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public String detectBrokenGame() {
        // return (this.team1 == null || this.team1.leader == null || this.team2 ==
        // null || this.team2.leader == null) || (this.isEnding && ((this.winner ==
        // null || this.winner.leader == null) || (this.loser == null ||
        // this.loser.leader == null)));

        if (this.team1 == null)
            return "Team 1 is not set [NULL]";
        if (this.team1.leader == null)
            return "Team 1 Leader is not set [NULL]";

        if (this.team2 == null)
            return "Team 2 is not set [NULL]";
        if (this.team2.leader == null)
            return "Team 2 Leader is not set [NULL]";

        if (this.isEnding) {
            if (this.winner == null)
                return "Winner Team is not set [NULL]";
            if (this.winner.leader == null)
                return "Winner Team Leader is not set [NULL]";

            if (this.loser == null)
                return "Loser Team is not set [NULL]";
            if (this.loser.leader == null)
                return "Loser Team Leader is not set [NULL]";
        }

        return null;

    }

    public static TeamDuelsGame startGame(@NotNull DuelsTeam team1, @NotNull DuelsTeam team2, String stringGameMode, @Nullable DuelsMap selectedMap) {
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            gameMode = DuelGameMode.CLASSIC;
            System.out.printf("[ERROR] Nexia: Invalid duel gamemode ({0}) selected! Using fallback one.%n", stringGameMode);
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

        for (ServerPlayer player : team1.all) {
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            DuelGameHandler.leave(player, false);

            data.gameMode = gameMode;
            data.teamDuelsGame = game;
            data.inDuel = true;

            player.setGameMode(GameType.ADVENTURE);
            selectedMap.p1Pos.teleportPlayer(duelLevel, player);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                            .append(Component.text(team2.leader.getScoreboardName() + "'s Team")
                                    .color(ChatFormat.brandColor2))));

            InventoryUtil.loadInventory(player, "duels-" + stringGameMode.toLowerCase());

            if (!gameMode.hasSaturation) {
                factoryPlayer.addTag(LobbyUtil.NO_SATURATION_TAG);
            }

            factoryPlayer.addTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            PlayerUtil.resetHealthStatus(factoryPlayer);
        }

        for (ServerPlayer player : team2.all) {
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            DuelGameHandler.leave(player, false);

            data.gameMode = gameMode;
            data.teamDuelsGame = game;
            data.inDuel = true;

            player.setGameMode(GameType.ADVENTURE);
            selectedMap.p2Pos.teleportPlayer(duelLevel, player);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false)
                            .append(Component.text(team1.leader.getScoreboardName() + "'s Team")
                                    .color(ChatFormat.brandColor2))));

            InventoryUtil.loadInventory(player, "duels-" + stringGameMode.toLowerCase());

            if (!gameMode.hasSaturation) {
                factoryPlayer.addTag(LobbyUtil.NO_SATURATION_TAG);
            }

            factoryPlayer.addTag(LobbyUtil.NO_DAMAGE_TAG);

            factoryPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            PlayerUtil.resetHealthStatus(factoryPlayer);
        }

        game.uuid = gameUUID;

        return game;
    }

    public void duelSecond() {
        String isBroken = this.detectBrokenGame();
        if (isBroken != null) {
            Component error = ChatFormat.nexiaMessage
                    .append(Component.text(
                                    "The game you were in was identified as broken, please contact NotCoded or any other dev.")
                            .color(ChatFormat.normalColor)
                            .decoration(ChatFormat.bold, false));

            Component errormsg = Component.text("Cause: " + isBroken);

            for (AccuratePlayer spectator : this.spectators) {
                Player factoryPlayer = PlayerUtil.getFactoryPlayer(spectator.get());
                factoryPlayer.sendMessage(error);
                factoryPlayer.sendMessage(errormsg);
            }

            for (ServerPlayer player : this.level.players()) {
                Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
                factoryPlayer.sendMessage(error);
                factoryPlayer.sendMessage(errormsg);
            }

            this.hasStarted = true;
            this.isEnding = true;
            this.shouldWait = false;

            DuelsTeam notNullTeam = this.team1;

            if(notNullTeam != null) notNullTeam = this.team2;
            if(notNullTeam != null) this.endGame(notNullTeam, null, false);
        }
        if (this.isEnding) {
            int color = 160 * 65536 + 248;
            // r * 65536 + g * 256 + b;
            DuelGameHandler.winnerRockets(this.winner.alive.get(new Random().nextInt(this.winner.alive.size())),
                    this.level, color);
            this.currentEndTime++;
            if (this.currentEndTime >= this.endTime || !this.shouldWait) {
                DuelsTeam winnerTeam = this.winner;
                DuelsTeam loserTeam = this.loser;

                for (AccuratePlayer spectator : this.spectators) {
                    PlayerUtil.getFactoryPlayer(spectator.get()).runCommand("/hub", 0, false);
                }

                this.isEnding = false;

                for (ServerPlayer player : loserTeam.all) {
                    PlayerDataManager.get(player).teamDuelsGame = null;
                    PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                }
                for (ServerPlayer player : winnerTeam.all) {
                    PlayerDataManager.get(player).teamDuelsGame = null;
                    PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                }

                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));
                this.team1.refreshTeam();
                this.team2.refreshTeam();
                DuelGameHandler.teamDuelsGames.remove(this);
                return;
            }
        }
        if (!this.hasStarted) {

            this.currentStartTime--;

            for (ServerPlayer player : this.team1.alive) {
                this.map.p1Pos.teleportPlayer(this.level, player);
            }
            for (ServerPlayer player : this.team2.alive) {
                this.map.p2Pos.teleportPlayer(this.level, player);
            }

            if (this.startTime - this.currentStartTime >= this.startTime) {

                for (ServerPlayer player : this.team1.alive) {
                    PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS,
                            10, 2);
                    player.setGameMode(this.gameMode.gameMode);
                    player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                for (ServerPlayer player : this.team2.alive) {
                    PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS,
                            10, 2);
                    player.setGameMode(this.gameMode.gameMode);
                    player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                this.hasStarted = true;
                return;
            }

            Title title;
            TextColor color = NamedTextColor.GREEN;

            if (this.currentStartTime <= 3 && this.currentStartTime > 1) {
                color = NamedTextColor.YELLOW;
            } else if (this.currentStartTime <= 1) {
                color = NamedTextColor.RED;
            }

            title = Title.title(Component.text(this.currentStartTime).color(color), Component.text(""),
                    Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));

            for (ServerPlayer player : this.team1.alive) {
                PlayerUtil.getFactoryPlayer(player).sendTitle(title);
                PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10,
                        1);
            }
            for (ServerPlayer player : this.team2.alive) {
                PlayerUtil.getFactoryPlayer(player).sendTitle(title);
                PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10,
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
                .append(Component.text("draw").color(ChatFormat.brandColor2))
                .append(Component.text("!").color(ChatFormat.normalColor));

        Component titleLose = Component.text("Draw")
                .color(ChatFormat.brandColor2);
        Component subtitleLose;

        Component titleWin = titleLose;
        Component subtitleWin = win;

        if ((winnerTeam == null || winnerTeam.leader == null)) {
            for (ServerPlayer player : loserTeam.all) {
                Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
                factoryPlayer.sendTitle(Title.title(titleWin, subtitleWin));
                factoryPlayer.sendMessage(win);
            }
            return;
        }

        win = Component.text(winnerTeam.leader.getScoreboardName() + "'s Team").color(ChatFormat.brandColor2)
                .append(Component.text(" has won the duel!").color(ChatFormat.normalColor));

        titleLose = Component.text("You lost!").color(ChatFormat.brandColor2);
        subtitleLose = Component.text("You have lost against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(winnerTeam.leader.getScoreboardName() + "'s Team")
                        .color(ChatFormat.brandColor2));

        titleWin = Component.text("You won!").color(ChatFormat.brandColor2);
        subtitleWin = Component.text("You have won against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(loserTeam.leader.getScoreboardName() + "'s Team")
                        .color(ChatFormat.brandColor2));

        for (ServerPlayer player : loserTeam.all) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerDataManager.get(player).savedData.loss++;
            factoryPlayer.sendTitle(Title.title(titleLose, subtitleLose));
            factoryPlayer.sendMessage(win);
        }

        for (ServerPlayer player : winnerTeam.all) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerDataManager.get(player).savedData.wins++;
            factoryPlayer.sendTitle(Title.title(titleWin, subtitleWin));
            factoryPlayer.sendMessage(win);
        }
    }

    public void death(@NotNull ServerPlayer victim, @Nullable DamageSource source) {
        PlayerData victimData = PlayerDataManager.get(victim);
        DuelsTeam victimTeam = victimData.duelsTeam;

        if (victimTeam == null)
            return;
        if (this.isEnding)
            return;

        victim.destroyVanishingCursedItems();
        victim.inventory.dropAll();
        victimTeam.alive.remove(victim);

        boolean isVictimTeamDead = victimTeam.alive.isEmpty();

        if (source != null && source.getEntity() instanceof ServerPlayer attacker) {
            PlayerData attackerData = PlayerDataManager.get(attacker);
            if (attackerData.teamDuelsGame != null && attackerData.teamDuelsGame.equals(this) && isVictimTeamDead) {
                this.endGame(victimTeam, attackerData.duelsTeam, true);
            }
            return;
        }
        if ((source == null || !(source.getEntity() instanceof ServerPlayer)) && isVictimTeamDead) {

            if (this.team1 == victimTeam)
                this.endGame(victimTeam, this.team2, true);
            else if (this.team2 == victimTeam)
                this.endGame(victimTeam, this.team1, true);

            return;
        }

        /*
         * if(isVictimTeamDead){
         * this.endGame(victimTeam, null, false);
         * }
         *
         */
    }
}