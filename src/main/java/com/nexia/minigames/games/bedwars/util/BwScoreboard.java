package com.nexia.minigames.games.bedwars.util;

import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.item.BlockUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.TickUtil;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.players.BwPlayers;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;

public class BwScoreboard {

    private static final String objectiveName = "bedwars_sidebar";
    private static final TextComponent title = new TextComponent(LegacyChatFormat.brandColor1 + "\247l    Bedwars    ");

    public static final int timerLine = 1;

    private static final Scoreboard scoreboard = new Scoreboard();
    private static final Objective objective = new Objective(
            scoreboard, objectiveName, ObjectiveCriteria.DUMMY,
            title, ObjectiveCriteria.RenderType.INTEGER);

    public static void setUpScoreboard() {
        for (NexiaPlayer player : BwPlayers.getPlayers()) {
            sendBedWarsScoreboard(player);
        }
        updateScoreboard();
    }

    public static void sendBedWarsScoreboard(NexiaPlayer player) {
        player.unwrap().connection.send(new ClientboundSetObjectivePacket(objective, 0));
        player.unwrap().connection.send(new ClientboundSetDisplayObjectivePacket(1, objective));
    }

    public static void updateScoreboard() {
        ServerLevel world = BwAreas.bedWarsWorld;
        ArrayList<String> lines = new ArrayList<>();

        lines.add(" ");
        for (BwTeam team : BwTeam.teamsInOrder) {
            String statusDisplay = team.textColor + "\247l» \247r" + team.displayName + " ";

            if (team.players.isEmpty()) {
                statusDisplay += "\247c✘";
            } else if (team.bedLocation != null && BlockUtil.blockToText(world.getBlockState(team.bedLocation)).endsWith("_bed")) {
                statusDisplay += "\247a✔";
            } else {
                statusDisplay += "\247a" + team.players.size();
            }

            lines.add(statusDisplay);
        }
        lines.add(" ");
        lines.add("  ");

        clearScoreboard();
        addLines(lines);
        updateTimer();

        for (ServerPlayer player : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            sendLines(new NexiaPlayer(player));
        }
    }

    public static void updateTimer() {
        int ticksLeft = BwGame.gameLength * 20 - BwGame.gameTicks - 1;
        String[] timer = TickUtil.minuteTimeStamp(ticksLeft);
        String line = "Time left: " + LegacyChatFormat.brandColor2 + timer[0] + ":" + timer[1];
        BwScoreboard.replaceLine(BwScoreboard.timerLine, line);
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

    private static void addLines(ArrayList<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int scoreNumber = lines.size() - i;

            scoreboard.getOrCreatePlayerScore(line, objective).setScore(scoreNumber);
        }
    }

    public static void sendLines(NexiaPlayer player) {
        for (Score score : scoreboard.getPlayerScores(objective)) {
            player.unwrap().connection.send(new ClientboundSetScorePacket(
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

    public static void removeScoreboardFor(NexiaPlayer player) {
        player.unwrap().connection.send(new ClientboundSetDisplayObjectivePacket(1, null));
    }

}
