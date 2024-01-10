package com.nexia.minigames.games.oitc.util;

import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.util.player.PlayerDataManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.notcoded.codelib.util.TickUtil;

import java.util.ArrayList;

public class OitcScoreboard {

    private static final String objectiveName = "oitc_sidebar";
    private static final TextComponent title = new TextComponent(LegacyChatFormat.brandColor1 + "\247l      OITC     ");

    public static final int timerLine = 1;

    private static final Scoreboard scoreboard = new Scoreboard();
    private static final Objective objective = new Objective(
            scoreboard, objectiveName, ObjectiveCriteria.DUMMY,
            title, ObjectiveCriteria.RenderType.INTEGER);

    public static void setUpScoreboard() {
        for (ServerPlayer player : OitcGame.getViewers()) {
            sendOITCScoreboard(player);
        }
        updateScoreboard();
    }

    public static void sendOITCScoreboard(ServerPlayer player) {
        player.connection.send(new ClientboundSetObjectivePacket(objective, 0));
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, objective));
    }

    public static void updateScoreboard() {
        ArrayList<String> lines = new ArrayList<>();

        lines.add(" ");
        lines.add(" ");
        lines.add("  ");

        clearScoreboard();
        addLines(lines);
        updateTimer();

        for (ServerPlayer player : OitcGame.getViewers()) {
            updateStats(player);
            sendLines(player);
        }
    }

    public static void updateTimer() {
        String[] timer = TickUtil.minuteTimeStamp(OitcGame.gameTime * 20);
        String time = "Time left: " + LegacyChatFormat.brandColor2 + timer[0] + ":" + timer[1];
        OitcScoreboard.replaceLine(OitcScoreboard.timerLine, time);
    }

    public static void updateStats(ServerPlayer player) {
        String kills = "Kills: " + LegacyChatFormat.brandColor2 + PlayerDataManager.get(player).kills;
        OitcScoreboard.replaceLinePlayer(2, kills, player);
    }

    public static void replaceLine(int i, String newLine) {
        for (Score score : scoreboard.getPlayerScores(objective)) {
            if (score.getScore() != i) continue;

            String previous = score.getOwner();

            scoreboard.resetPlayerScore(previous, objective);
            scoreboard.getOrCreatePlayerScore(newLine, objective).setScore(i);

            for (ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundSetScorePacket(
                        ServerScoreboard.Method.REMOVE, objectiveName, previous, i));
                player.connection.send(new ClientboundSetScorePacket(
                        ServerScoreboard.Method.CHANGE, objectiveName, newLine, i));
            }
        }
    }

    public static void replaceLinePlayer(int i, String newLine, ServerPlayer player){
        for(Score score : scoreboard.getPlayerScores(objective)){
            if (score.getScore() != i) continue;

            String previous = score.getOwner();

            scoreboard.resetPlayerScore(previous, objective);
            scoreboard.getOrCreatePlayerScore(newLine, objective).setScore(i);

            player.connection.send(new ClientboundSetScorePacket(
                    ServerScoreboard.Method.REMOVE, objectiveName, previous, i));
            player.connection.send(new ClientboundSetScorePacket(
                    ServerScoreboard.Method.CHANGE, objectiveName, newLine, i));
        }
    }

    private static void addLines(ArrayList<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int scoreNumber = lines.size() - i;

            scoreboard.getOrCreatePlayerScore(line, objective).setScore(scoreNumber);
        }
    }

    public static void sendLines(ServerPlayer player) {
        for (Score score : scoreboard.getPlayerScores(objective)) {
            player.connection.send(new ClientboundSetScorePacket(
                    ServerScoreboard.Method.CHANGE, objectiveName, score.getOwner(), score.getScore()));
        }
    }

    private static void clearScoreboard() {
        for (Score score : scoreboard.getPlayerScores(objective)) {
            for (ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundSetScorePacket(
                        ServerScoreboard.Method.REMOVE, objectiveName, score.getOwner(), score.getScore()));
            }
            scoreboard.resetPlayerScore(score.getOwner(), objective);
        }
    }

    public static void removeScoreboardFor(ServerPlayer player) {
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, null));
    }

}