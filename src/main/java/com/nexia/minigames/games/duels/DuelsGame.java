package com.nexia.minigames.games.duels;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelsGame { //implements Runnable{
    public ServerPlayer p1;

    public UUID uuid;
    public ServerPlayer p2;

    public DuelGameMode gameMode;

    public DuelsMap map;

    public boolean isEnding = false;

    public boolean hasStarted = false;

    public int startTime;

    private int currentStartTime = 5;

    public int endTime;

    private int currentEndTime = 0;

    public ServerLevel level;

    public ArrayList<ServerPlayer> spectators = new ArrayList<>();


    // Winner thingie
    public ServerPlayer winner = null;

    public ServerPlayer loser = null;

    private boolean shouldWait = false;

    public DuelsGame(ServerPlayer p1, ServerPlayer p2, DuelGameMode gameMode, DuelsMap map, ServerLevel level, int endTime, int startTime){
        this.p1 = p1;
        this.p2 = p2;
        this.gameMode = gameMode;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public static DuelsGame startGame(ServerPlayer mcP1, ServerPlayer mcP2, String stringGameMode, @Nullable DuelsMap selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.FFA;
            System.out.printf("[ERROR] Nexia: Invalid duel gamemode ({0}) selected! Using fallback one.%n", stringGameMode);
            stringGameMode = "FFA";
        }

        PlayerData invitorData = PlayerDataManager.get(mcP1);
        PlayerData playerData = PlayerDataManager.get(mcP2);

        if(invitorData.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(mcP1, invitorData.spectatingPlayer, false);
        }
        if(playerData.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(mcP2, playerData.spectatingPlayer, false);
        }


        Player p1 = PlayerUtil.getFactoryPlayer(mcP1);
        Player p2 = PlayerUtil.getFactoryPlayer(mcP2);

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), gameMode.hasRegen);
        if(selectedMap == null){
            selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
        }

        String name = String.valueOf(gameUUID);

        /*
        String mapid = "duels";
        String start = "/execute in " + mapid + ":" + name;
        ServerTime.factoryServer.runCommand(start + " run forceload add 0 0");
        ServerTime.factoryServer.runCommand(start + " run " + selectedMap.structureMap.returnCommand(duelLevel));
        ServerTime.factoryServer.runCommand(start + " run setblock 1 80 0 minecraft:redstone_block");

        ServerTime.factoryServer.runCommand(start + " if block 0 80 0 minecraft:structure_block run setblock 0 80 0 air");
        ServerTime.factoryServer.runCommand(start + " if block 1 80 0 minecraft:redstone_block run setblock 1 80 0 air");
         */

        selectedMap.structureMap.pasteMap(duelLevel);

        if(!gameMode.hasSaturation) {
            p1.addTag(LobbyUtil.NO_SATURATION_TAG);
            p2.addTag(LobbyUtil.NO_SATURATION_TAG);
        }

        PlayerUtil.resetHealthStatus(p1);
        PlayerUtil.resetHealthStatus(p2);

        selectedMap.p2Pos.teleportPlayer(duelLevel, mcP2);
        playerData.inviting = false;
        playerData.invitingPlayer = null;
        playerData.inDuel = true;
        removeQueue(mcP2, null, true);
        playerData.spectatingPlayer = null;
        playerData.duelPlayer = mcP1;

        selectedMap.p1Pos.teleportPlayer(duelLevel, mcP1);
        invitorData.inviting = false;
        invitorData.invitingPlayer = null;
        invitorData.inDuel = true;
        removeQueue(mcP2, null, true);
        invitorData.spectatingPlayer = null;
        invitorData.duelPlayer = mcP2;

        mcP1.setGameMode(gameMode.gameMode);
        mcP2.setGameMode(gameMode.gameMode);

        removeQueue(mcP1, null, true);
        removeQueue(mcP2, null, true);

        /*
        InventoryUtil.setInventory(player, stringGameMode.toLowerCase(), "/duels", true);
        InventoryUtil.setInventory(invitor, stringGameMode.toLowerCase(), "/duels", true);
         */


        p2.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p1.getRawName()).color(ChatFormat.brandColor2))));

        p1.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p2.getRawName()).color(ChatFormat.brandColor2))));

        p1.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);
        p2.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;

        DuelsGame game = new DuelsGame(mcP1, mcP2, gameMode, selectedMap, duelLevel, 5, 5);
        invitorData.duelsGame = game;
        playerData.duelsGame = game;

        DuelGameHandler.duelsGames.add(game);

        game.uuid = gameUUID;

        return game;
    }

    public void duelSecond() {
        if(this.isEnding) {
            int color = 160 * 65536 + 248;
            // r * 65536 + g * 256 + b;
            DuelGameHandler.winnerRockets(this.winner, this.level, color);
            this.currentEndTime++;
            if(this.currentEndTime >= this.endTime || !this.shouldWait) {
                ServerPlayer minecraftAttacker = this.winner;
                ServerPlayer minecraftVictim = this.loser;
                Player attacker = PlayerUtil.getFactoryPlayer(minecraftAttacker);

                PlayerData victimData = PlayerDataManager.get(minecraftVictim);
                PlayerData attackerData = PlayerDataManager.get(minecraftAttacker);

                PlayerUtil.resetHealthStatus(attacker);

                for(ServerPlayer spectator : this.spectators) {
                    PlayerUtil.getFactoryPlayer(spectator).runCommand("/hub", 0, false);
                }

                victimData.duelsGame = null;
                victimData.inviting = false;
                victimData.inDuel = false;
                victimData.inviteMap = DuelsMap.CITY;
                victimData.inviteKit = "";
                removeQueue(minecraftVictim, null, true);
                victimData.gameMode = DuelGameMode.LOBBY;

                attackerData.duelsGame = null;
                attackerData.inviting = false;
                attackerData.inDuel = false;
                attackerData.inviteMap = DuelsMap.CITY;
                attackerData.inviteKit = "";
                removeQueue(minecraftAttacker, null, true);
                attackerData.gameMode = DuelGameMode.LOBBY;

                attackerData.savedData.wins++;
                victimData.savedData.loss++;

                this.isEnding = false;

                minecraftVictim = PlayerUtil.getFixedPlayer(minecraftVictim);

                if(minecraftVictim != null) {
                    PlayerUtil.getFactoryPlayer(minecraftVictim).runCommand("/hub", 0, false);
                }


                minecraftAttacker = PlayerUtil.getFixedPlayer(minecraftAttacker);

                if(minecraftAttacker != null) {
                    PlayerUtil.getFactoryPlayer(minecraftAttacker).runCommand("/hub", 0, false);
                }

                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));

                DuelGameHandler.duelsGames.remove(this);
                return;
            }
        }
        if(!this.hasStarted) {

            this.currentStartTime--;

            this.map.p1Pos.teleportPlayer(this.level, this.p1);
            this.map.p2Pos.teleportPlayer(this.level, this.p2);

            if (this.startTime - this.currentStartTime >= this.startTime) {
                PlayerUtil.sendSound(this.p1, new EntityPos(this.p1), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                PlayerUtil.sendSound(this.p2, new EntityPos(this.p2), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                this.p1.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                this.p1.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

                this.p2.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                this.p2.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
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

            PlayerUtil.getFactoryPlayer(this.p1).sendTitle(title);
            PlayerUtil.getFactoryPlayer(this.p2).sendTitle(title);

            PlayerUtil.sendSound(this.p1, new EntityPos(this.p1), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
            PlayerUtil.sendSound(this.p2, new EntityPos(this.p2), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);

        }
    }

    public void endGame(@NotNull ServerPlayer minecraftVictim, @Nullable ServerPlayer minecraftAttacker, boolean wait) {

        this.winner = minecraftAttacker;
        this.loser = minecraftVictim;
        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;

        boolean attackerNull = minecraftAttacker == null;

        Player victim = PlayerUtil.getFactoryPlayer(minecraftVictim);
        Player attacker = null;
        if (!attackerNull) {
            attacker = PlayerUtil.getFactoryPlayer(minecraftAttacker);
        }

        Component win = Component.text("The game was a ")
                .color(ChatFormat.normalColor)
                .append(Component.text("draw").color(ChatFormat.brandColor2))
                .append(Component.text("!").color(ChatFormat.normalColor)
                );

        Component titleLose = Component.text("Draw")
                .color(ChatFormat.brandColor2);
        Component subtitleLose = win;

        Component titleWin;
        Component subtitleWin;


        if (!attackerNull) {
            win = Component.text(attacker.getRawName()).color(ChatFormat.brandColor2)
                    .append(Component.text(" has won the duel!").color(ChatFormat.normalColor)
                            .append(Component.text(" [")
                                    .color(ChatFormat.lineColor))
                            .append(Component.text(FfaUtil.calculateHealth(attacker.getHealth()) + "❤").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                    );

            titleLose = Component.text("You lost!").color(ChatFormat.brandColor2);
            subtitleLose = Component.text("You have lost against ")
                    .color(ChatFormat.normalColor)
                    .append(Component.text(attacker.getRawName())
                            .color(ChatFormat.brandColor2)
                            .append(Component.text(" [")
                                    .color(ChatFormat.lineColor)
                            .append(Component.text(FfaUtil.calculateHealth(attacker.getHealth()) + "❤").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            )
                    );

            titleWin = Component.text("You won!").color(ChatFormat.brandColor2);
            subtitleWin = Component.text("You have won against ")
                    .color(ChatFormat.normalColor)
                    .append(Component.text(victim.getRawName())
                            .color(ChatFormat.brandColor2)
                    );

            attacker.sendMessage(win);
            attacker.sendTitle(Title.title(titleWin, subtitleWin));
        }
        victim.sendMessage(win);
        victim.sendTitle(Title.title(titleLose, subtitleLose));
    }

    public void death(@NotNull ServerPlayer victim, @Nullable DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(victimData.duelsGame == null) return;
        if(victimData.duelsGame.isEnding) return;

        if(source != null && source.getEntity() instanceof ServerPlayer attacker){
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if((victimData.inDuel && attackerData.inDuel) && victimData.duelsGame == attackerData.duelsGame){
                endGame(victim, attacker, true);
            }
            return;
        }
        if((source == null || !(source.getEntity() instanceof ServerPlayer)) && PlayerDataManager.get(victim).duelPlayer != null) {
            ServerPlayer attacker = victimData.duelPlayer;
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if ((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                endGame(victim, attacker, true);
            }
            return;
        }

        if(victimData.inDuel) {
            endGame(victim, null, false);
        }
    }
}