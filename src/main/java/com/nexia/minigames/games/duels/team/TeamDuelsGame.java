package com.nexia.minigames.games.duels.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
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
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
public class TeamDuelsGame { //implements Runnable{
    public DuelsTeam team1;

    public DuelsTeam team2;

    public DuelGameMode gameMode;

    public String selectedMap;

    public boolean isEnding = false;

    public boolean hasStarted = false;

    public HashMap<DuelsTeam, float[]> spawnPositions = new HashMap<>();

    public int startTime;

    private int currentStartTime = 5;

    public int endTime;

    private int currentEndTime = 0;

    public ServerLevel level;

    public ArrayList<ServerPlayer> spectators = new ArrayList<>();


    // Winner thingie
    public DuelsTeam winner = null;

    public DuelsTeam loser = null;

    private boolean shouldWait = false;

    public TeamDuelsGame(DuelsTeam team1, DuelsTeam team2, DuelGameMode gameMode, String selectedMap, ServerLevel level, int endTime, int startTime){
        this.team1 = team1;
        this.team2 = team2;
        this.gameMode = gameMode;
        this.selectedMap = selectedMap;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public static TeamDuelsGame startGame(@NotNull DuelsTeam team1, @NotNull DuelsTeam team2, String stringGameMode, @Nullable String selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.FFA;
            stringGameMode = "FFA";
            System.out.printf("[ERROR] Nexia: Invalid duel gamemode ({0}) selected! Using fallback one.%n", stringGameMode);
        }

        team1.alive.clear();
        team1.alive.addAll(team1.all);

        team2.alive.clear();
        team2.alive.addAll(team2.all);

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameMode.hasRegen);
        if(selectedMap == null){
            selectedMap = com.nexia.minigames.Main.config.duelsMaps.get(RandomUtil.randomInt(0, com.nexia.minigames.Main.config.duelsMaps.size()));
        }
        String name = duelLevel.dimension().toString().replaceAll("]", "").split(":")[2];

        String mapid = "duels";

        String start = "/execute in " + mapid + ":" + name;

        ServerTime.factoryServer.runCommand(start + " run forceload add 0 0");
        ServerTime.factoryServer.runCommand(start + " run " + DuelGameHandler.returnCommandMap(selectedMap));
        ServerTime.factoryServer.runCommand(start + " run setblock 1 80 0 minecraft:redstone_block");

        ServerTime.factoryServer.runCommand(start + " if block 0 80 0 minecraft:structure_block run setblock 0 80 0 air");
        ServerTime.factoryServer.runCommand(start + " if block 1 80 0 minecraft:redstone_block run setblock 1 80 0 air");

        float[] team1Pos = DuelGameHandler.returnPosMap(selectedMap, true);
        float[] team2Pos = DuelGameHandler.returnPosMap(selectedMap, false);

        TeamDuelsGame game = new TeamDuelsGame(team1, team2, gameMode, selectedMap, duelLevel, 5, 5);
        DuelGameHandler.teamDuelsGames.add(game);

        for(ServerPlayer player : team1.all) {
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            DuelGameHandler.leave(player, false);

            data.gameMode = gameMode;
            data.teamDuelsGame = game;
            data.inDuel = true;

            player.teleportTo(duelLevel, team1Pos[0], team1Pos[1], team1Pos[2], team1Pos[3], team1Pos[4]);
            player.setGameMode(gameMode.gameMode);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(team2.creator.getScoreboardName() + "'s Team").color(ChatFormat.brandColor2))));

            factoryPlayer.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);

            factoryPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            PlayerUtil.resetHealthStatus(factoryPlayer);
        }

        for(ServerPlayer player : team2.all) {
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            DuelGameHandler.leave(player, false);

            data.gameMode = gameMode;
            data.teamDuelsGame = game;
            data.inDuel = true;

            player.teleportTo(duelLevel, team2Pos[0], team2Pos[1], team2Pos[2], team2Pos[3], team2Pos[4]);
            player.setGameMode(gameMode.gameMode);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(team1.creator.getScoreboardName() + "'s Team").color(ChatFormat.brandColor2))));

            factoryPlayer.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);

            factoryPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            PlayerUtil.resetHealthStatus(factoryPlayer);
        }

        game.spawnPositions.put(team1, team1Pos);
        game.spawnPositions.put(team2, team2Pos);


        return game;
    }

    public void duelSecond() {
        if(this.isEnding) {
            int color = 160 * 65536 + 248;
            // r * 65536 + g * 256 + b;
            DuelGameHandler.winnerRockets(this.winner.alive.get(new Random().nextInt(this.winner.alive.size())), this.level, color);
            this.currentEndTime++;
            if(this.currentEndTime >= this.endTime || !this.shouldWait) {
                DuelsTeam winnerTeam = this.winner;
                DuelsTeam loserTeam = this.loser;

                for(ServerPlayer spectator : this.spectators) {
                    PlayerUtil.getFactoryPlayer(spectator).runCommand("/hub", 0, false);
                }

                this.isEnding = false;

                for(ServerPlayer player : loserTeam.all) {
                    PlayerDataManager.get(player).teamDuelsGame = null;
                    PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                }
                for(ServerPlayer player : winnerTeam.all) {
                    PlayerDataManager.get(player).teamDuelsGame = null;
                    PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                }

                DuelGameHandler.deleteWorld(this.level.dimension().toString().replaceAll("]", "").split(":")[2]);
                DuelGameHandler.teamDuelsGames.remove(this);
                return;
            }
        }
        if(!this.hasStarted) {
            float[] team1pos = this.spawnPositions.get(this.team1);
            float[] team2pos = this.spawnPositions.get(this.team2);

            this.currentStartTime--;

            for(ServerPlayer player : this.team1.alive) {
                player.teleportTo(this.level, team1pos[0], team1pos[1], team1pos[2], team1pos[3], team1pos[4]);
            }
            for(ServerPlayer player : this.team2.alive) {
                player.teleportTo(this.level, team2pos[0], team2pos[1], team2pos[2], team2pos[3], team2pos[4]);
            }

            if (this.startTime - this.currentStartTime >= this.startTime) {

                for(ServerPlayer player : this.team1.alive) {
                    PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                    player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                for(ServerPlayer player : this.team2.alive) {
                    PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                    player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                    player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                }
                this.hasStarted = true;
                return;
            }

            Title title;
            TextColor color = NamedTextColor.GREEN;

            if(this.currentStartTime <= 3 && this.currentStartTime > 1) {
                color = NamedTextColor.YELLOW;
            } else if(this.currentStartTime <= 1) {
                color = NamedTextColor.RED;
            }

            title = Title.title(Component.text(this.currentStartTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));

            for(ServerPlayer player : this.team1.alive) {
                PlayerUtil.getFactoryPlayer(player).sendTitle(title);
                PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
            }
            for(ServerPlayer player : this.team2.alive) {
                PlayerUtil.getFactoryPlayer(player).sendTitle(title);
                PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
            }
        }
    }

    public void endGame(@NotNull DuelsTeam loserTeam, @Nullable DuelsTeam winnerTeam, boolean wait) {

        this.winner = winnerTeam;
        this.loser = loserTeam;
        if(winnerTeam == null) {
            this.winner = this.team1;
            if(loserTeam == this.team1) {
                this.winner = this.team2;
                this.loser = this.team1;
            } else if(loserTeam == this.team2){
                this.loser = this.team2;
            }
        }


        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;


        Component win = Component.text("The game was a ")
                .color(ChatFormat.normalColor)
                .append(Component.text("draw").color(ChatFormat.brandColor2))
                .append(Component.text("!").color(ChatFormat.normalColor)
                );

        Component titleLose = Component.text("Draw")
                .color(ChatFormat.brandColor2);
        Component subtitleLose = win;

        Component titleWin = titleLose;
        Component subtitleWin = win;


        if (winnerTeam == null) {
            for(ServerPlayer player : loserTeam.all) {
                Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
                factoryPlayer.sendTitle(Title.title(titleWin, subtitleWin));
                factoryPlayer.sendMessage(win);
            }
            return;
        }


        win = Component.text(winnerTeam.creator.getScoreboardName() + "'s Team").color(ChatFormat.brandColor2)
                .append(Component.text(" has won the duel!").color(ChatFormat.normalColor)
                );

        titleLose = Component.text("You lost!").color(ChatFormat.brandColor2);
        subtitleLose = Component.text("You have lost against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(winnerTeam.creator.getScoreboardName() + "'s Team")
                        .color(ChatFormat.brandColor2)
                );

        titleWin = Component.text("You won!").color(ChatFormat.brandColor2);
        subtitleWin = Component.text("You have won against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(loserTeam.creator.getScoreboardName() + "'s Team")
                        .color(ChatFormat.brandColor2)
                );

        for(ServerPlayer player : loserTeam.all) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerDataManager.get(player).savedData.loss++;
            factoryPlayer.sendTitle(Title.title(titleLose, subtitleLose));
            factoryPlayer.sendMessage(win);
        }

        for(ServerPlayer player : winnerTeam.all) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerDataManager.get(player).savedData.wins++;
            factoryPlayer.sendTitle(Title.title(titleWin, subtitleWin));
            factoryPlayer.sendMessage(win);
        }
    }

    public void death(@NotNull ServerPlayer victim, @Nullable DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        DuelsTeam victimTeam = victimData.duelsTeam;

        if(victimTeam == null) return;
        if(this.isEnding) return;

        victimTeam.alive.remove(victim);

        boolean isVictimTeamDead = victimTeam.alive.isEmpty();

        if(source != null && source.getEntity() instanceof ServerPlayer attacker){
            PlayerData attackerData = PlayerDataManager.get(attacker);
            if(attackerData.teamDuelsGame.equals(this) && isVictimTeamDead) {
                this.endGame(victimTeam, attackerData.duelsTeam, true);
            }
            return;
        }
        if(source == null || !(source.getEntity() instanceof ServerPlayer)) {

            if(this.team1 == victimTeam) this.endGame(victimTeam, this.team2, true);
            else if(this.team2 == victimTeam) this.endGame(victimTeam, this.team1, true);

            return;
        }

        if(isVictimTeamDead){
            this.endGame(victimTeam, null, false);
        }
    }
}