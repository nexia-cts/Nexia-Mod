package com.nexia.minigames.games.duels.team;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamDuelsGame { //implements Runnable{
    public DuelsTeam team1;

    public DuelsTeam team2;

    public DuelGameMode gameMode;

    public String selectedMap;

    public ServerLevel level;

    public ArrayList<ServerPlayer> spectators = new ArrayList<>();

    public TeamDuelsGame(DuelsTeam team1, DuelsTeam team2, DuelGameMode gameMode, String selectedMap, ServerLevel level){
        this.team1 = team1;
        this.team2 = team2;
        this.gameMode = gameMode;
        this.selectedMap = selectedMap;
        this.level = level;
    }

    public static TeamDuelsGame startGame(DuelsTeam team1, DuelsTeam team2, String stringGameMode, @Nullable String selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.FFA;
            System.out.printf("[ERROR] Nexia: Invalid duel gamemode (%s) selected! Using fallback one.%n", stringGameMode);
        }

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

        TeamDuelsGame game = new TeamDuelsGame(team1, team2, gameMode, selectedMap, duelLevel);
        DuelGameHandler.teamDuelsGames.add(game);

        for(ServerPlayer player : team1.all) {
            PlayerData data = PlayerDataManager.get(player);
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            DuelGameHandler.leave(player);

            data.gameMode = gameMode;
            data.teamDuelsGame = game;
            data.inDuel = true;

            player.teleportTo(duelLevel, team1Pos[0], team1Pos[1], team1Pos[2], team1Pos[3], team1Pos[4]);
            player.setGameMode(gameMode.gameMode);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage()
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

            DuelGameHandler.leave(player);

            data.gameMode = gameMode;
            data.teamDuelsGame = game;
            data.inDuel = true;

            player.teleportTo(duelLevel, team2Pos[0], team2Pos[1], team2Pos[2], team2Pos[3], team2Pos[4]);
            player.setGameMode(gameMode.gameMode);

            factoryPlayer.sendMessage(ChatFormat.nexiaMessage()
                    .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(team1.creator.getScoreboardName() + "'s Team").color(ChatFormat.brandColor2))));

            factoryPlayer.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);

            factoryPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            factoryPlayer.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

            PlayerUtil.resetHealthStatus(factoryPlayer);
        }

        return game;
    }

    public void endGame(@NotNull DuelsTeam teamVictim, @Nullable DuelsTeam teamAttacker, boolean wait) {

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

        if(teamAttacker == null) {
            for(ServerPlayer player : teamVictim.all) {
                Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

                factoryPlayer.sendMessage(win);
                factoryPlayer.sendTitle(Title.title(titleWin, subtitleWin));

                PlayerUtil.resetHealthStatus(factoryPlayer);

                //LobbyUtil.sendGame(minecraftAttacker, "duels", false, false);

                factoryPlayer.getInventory().clear();
                factoryPlayer.setGameMode(Minecraft.GameMode.ADVENTURE);
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for(ServerPlayer player : teamVictim.all) {
                        LobbyUtil.leaveAllGames(player, true);
                    }
                }
            }, 100L);
            return;
        }


        win = Component.text(teamAttacker.creator.getScoreboardName() + "'s Team").color(ChatFormat.brandColor2)
                .append(Component.text(" has won the duel!").color(ChatFormat.normalColor)
                );

        titleLose = Component.text("You lost!").color(ChatFormat.brandColor2);
        subtitleLose = Component.text("You have lost against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(teamAttacker.creator.getScoreboardName() + "'s Team")
                        .color(ChatFormat.brandColor2)
                );

        titleWin = Component.text("You won!").color(ChatFormat.brandColor2);
        subtitleWin = Component.text("You have won against ")
                .color(ChatFormat.normalColor)
                .append(Component.text(teamVictim.creator.getScoreboardName() + "'s Team")
                        .color(ChatFormat.brandColor2)
                );



        for(ServerPlayer player : teamAttacker.all) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            factoryPlayer.sendMessage(win);
            factoryPlayer.sendTitle(Title.title(titleWin, subtitleWin));
            player.die(DamageSource.GENERIC);

            PlayerUtil.resetHealthStatus(factoryPlayer);

            //LobbyUtil.sendGame(minecraftAttacker, "duels", false, false);

            factoryPlayer.getInventory().clear();
            factoryPlayer.setGameMode(Minecraft.GameMode.ADVENTURE);
        }

        for(ServerPlayer player : teamVictim.all) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            factoryPlayer.sendMessage(win);
            factoryPlayer.sendTitle(Title.title(titleLose, subtitleLose));

            PlayerUtil.resetHealthStatus(factoryPlayer);

            //LobbyUtil.sendGame(minecraftAttacker, "duels", false, false);

            factoryPlayer.getInventory().clear();
            factoryPlayer.setGameMode(Minecraft.GameMode.ADVENTURE);
        }

        /*
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<ServerPlayer> allTeams = teamVictim.all;
                allTeams.addAll(teamAttacker.all);
                for(ServerPlayer player : allTeams) {
                    LobbyUtil.leaveAllGames(player, true);
                }
            }
        }, 100L);

         */

        for(ServerPlayer spectator : this.spectators) {
            LobbyUtil.returnToLobby(spectator, true);
        }

        DuelGameHandler.teamDuelsGames.remove(this);

        DuelGameHandler.deleteWorld(this.level.dimension().toString().replaceAll("]", "").split(":")[2]);
    }

    public void death(@NotNull ServerPlayer victim, @Nullable DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        DuelsTeam victimTeam = victimData.duelsTeam;

        victimTeam.alive.remove(victim);

        boolean isVictimTeamDead = victimTeam.alive.isEmpty();

        if(source != null && source.getEntity() instanceof ServerPlayer attacker){
            PlayerData attackerData = PlayerDataManager.get(attacker);
            if(attackerData.teamDuelsGame.equals(victimData.teamDuelsGame) && isVictimTeamDead) {
                endGame(victimTeam, attackerData.duelsTeam, true);
            }
            return;
        }
        if(source == null || !(source.getEntity() instanceof ServerPlayer)) {
            TeamDuelsGame game = victimData.teamDuelsGame;

            if(game.team1 == victimTeam) endGame(victimTeam, team2, true);
            else if(game.team2 == victimTeam) endGame(victimTeam, team1, true);

            return;
        }

        if(isVictimTeamDead){
            endGame(victimTeam, null, false);
        }
    }
}