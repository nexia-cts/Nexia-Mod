package com.nexia.minigames.games.duels;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.util.DuelOptions;
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

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelsGame {
    public AccuratePlayer p1;

    public UUID uuid;
    public AccuratePlayer p2;

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
    public AccuratePlayer winner = null;

    public AccuratePlayer loser = null;

    private boolean shouldWait = false;

    public DuelsGame(ServerPlayer p1, ServerPlayer p2, DuelGameMode gameMode, DuelsMap map, ServerLevel level, int endTime, int startTime){
        this.p1 = AccuratePlayer.create(p1);
        this.p2 = AccuratePlayer.create(p2);
        this.gameMode = gameMode;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public static DuelsGame startGame(ServerPlayer mcP1, ServerPlayer mcP2, String stringGameMode, @Nullable DuelsMap selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.CLASSIC;
            Main.logger.error(String.format("[Nexia]: Invalid duel gamemode (%s) selected! Using fallback one.", stringGameMode));
            stringGameMode = "CLASSIC";
        }

        PlayerData invitorData = PlayerDataManager.get(mcP1);
        PlayerData playerData = PlayerDataManager.get(mcP2);

        if(invitorData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(AccuratePlayer.create(mcP1), invitorData.duelOptions.spectatingPlayer, false);
        }
        if(playerData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(AccuratePlayer.create(mcP2), playerData.duelOptions.spectatingPlayer, false);
        }


        Player p1 = PlayerUtil.getFactoryPlayer(mcP1);
        Player p2 = PlayerUtil.getFactoryPlayer(mcP2);

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), gameMode.hasRegen);
        if(selectedMap == null){
            selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
            while(!selectedMap.isAdventureSupported && gameMode.gameMode.equals(GameType.ADVENTURE)) {
                selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
            }
        }

        selectedMap.structureMap.pasteMap(duelLevel);

        if(!gameMode.hasSaturation) {
            p1.addTag(LobbyUtil.NO_SATURATION_TAG);
            p2.addTag(LobbyUtil.NO_SATURATION_TAG);
        }

        PlayerUtil.resetHealthStatus(p1);
        PlayerUtil.resetHealthStatus(p2);

        selectedMap.p2Pos.teleportPlayer(duelLevel, mcP2);
        playerData.inviteOptions.reset();
        playerData.inDuel = true;
        removeQueue(mcP2, null, true);
        playerData.duelOptions.spectatingPlayer = null;

        selectedMap.p1Pos.teleportPlayer(duelLevel, mcP1);
        invitorData.inviteOptions.inviting = false;
        invitorData.inviteOptions.invitingPlayer = null;
        invitorData.inDuel = true;
        removeQueue(mcP2, null, true);
        invitorData.duelOptions.spectatingPlayer = null;
        mcP1.setGameMode(GameType.ADVENTURE);
        mcP2.setGameMode(GameType.ADVENTURE);

        removeQueue(mcP1, null, true);
        removeQueue(mcP2, null, true);


        p2.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p1.getRawName()).color(ChatFormat.brandColor2))));

        p1.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p2.getRawName()).color(ChatFormat.brandColor2))));

        File p1File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + mcP1.getStringUUID() + File.separator + "layout", stringGameMode.toLowerCase() + ".txt");
        if(p1File.exists()) InventoryUtil.loadInventory(mcP1, "duels/custom/" + mcP1.getStringUUID() + "/layout", stringGameMode.toLowerCase());
        else InventoryUtil.loadInventory(mcP1, "duels", stringGameMode.toLowerCase());

        File p2File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + mcP2.getStringUUID() + File.separator + "layout", stringGameMode.toLowerCase() + ".txt");
        if(p2File.exists()) InventoryUtil.loadInventory(mcP2, "duels/custom/" + mcP2.getStringUUID() + "/layout", stringGameMode.toLowerCase());
        else InventoryUtil.loadInventory(mcP2, "duels", stringGameMode.toLowerCase());

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;

        DuelsGame game = new DuelsGame(mcP1, mcP2, gameMode, selectedMap, duelLevel, 5, 5);

        playerData.gameOptions = new DuelOptions.GameOptions(game, AccuratePlayer.create(mcP1));
        invitorData.gameOptions = new DuelOptions.GameOptions(game, AccuratePlayer.create(mcP2));

        DuelGameHandler.duelsGames.add(game);

        game.uuid = gameUUID;

        return game;
    }

    public void duelSecond() {
        if(this.isEnding) {
            int color = 160 * 65536 + 248;
            // r * 65536 + g * 256 + b;
            DuelGameHandler.winnerRockets(this.winner.get(), this.level, color);
            this.currentEndTime++;
            if(this.currentEndTime >= this.endTime || !this.shouldWait) {
                AccuratePlayer minecraftAttacker = this.winner;
                AccuratePlayer minecraftVictim = this.loser;
                Player attacker = PlayerUtil.getFactoryPlayer(minecraftAttacker.get());

                PlayerData victimData = PlayerDataManager.get(minecraftVictim.get());
                PlayerData attackerData = PlayerDataManager.get(minecraftAttacker.get());

                PlayerUtil.resetHealthStatus(attacker);

                for(AccuratePlayer spectator : this.spectators) {
                    PlayerUtil.getFactoryPlayer(spectator.get()).runCommand("/hub", 0, false);
                }

                victimData.gameOptions = null;
                victimData.inDuel = false;
                removeQueue(minecraftVictim.get(), null, true);
                victimData.gameMode = DuelGameMode.LOBBY;
                victimData.inviteOptions.reset();
                victimData.duelOptions.spectatingPlayer = null;

                attackerData.gameOptions = null;
                attackerData.inDuel = false;
                removeQueue(minecraftAttacker.get(), null, true);
                attackerData.gameMode = DuelGameMode.LOBBY;
                attackerData.inviteOptions.reset();
                attackerData.duelOptions.spectatingPlayer = null;

                attackerData.savedData.wins++;
                victimData.savedData.loss++;

                this.isEnding = false;

                if(minecraftVictim.get() != null) {
                    PlayerUtil.getFactoryPlayer(minecraftVictim.get()).runCommand("/hub", 0, false);
                }

                if(minecraftAttacker.get() != null) {
                    PlayerUtil.getFactoryPlayer(minecraftAttacker.get()).runCommand("/hub", 0, false);
                }

                for(ServerPlayer spectator : this.level.players()) {
                    PlayerUtil.getFactoryPlayer(spectator).runCommand("/hub", 0, false);
                    spectator.kill();
                }

                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));
                DuelGameHandler.duelsGames.remove(this);
                return;
            }
        }
        if(!this.hasStarted) {

            this.currentStartTime--;

            ServerPlayer p1 = this.p1.get();
            ServerPlayer p2 = this.p2.get();

            this.map.p1Pos.teleportPlayer(this.level, p1);
            this.map.p2Pos.teleportPlayer(this.level, p2);

            if (this.startTime - this.currentStartTime >= this.startTime) {
                PlayerUtil.sendSound(p1, new EntityPos(p1), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                PlayerUtil.sendSound(p2, new EntityPos(p2), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                p1.setGameMode((this.gameMode != null) ? this.gameMode.gameMode : GameType.SURVIVAL);
                p1.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                p1.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

                p2.setGameMode((this.gameMode != null) ? this.gameMode.gameMode : GameType.SURVIVAL);
                p2.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                p2.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
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

            PlayerUtil.getFactoryPlayer(p1).sendTitle(title);
            PlayerUtil.getFactoryPlayer(p2).sendTitle(title);

            PlayerUtil.sendSound(p1, new EntityPos(p1), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
            PlayerUtil.sendSound(p2, new EntityPos(p2), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);

        }
    }

    public void endGame(@NotNull ServerPlayer minecraftVictim, @Nullable ServerPlayer minecraftAttacker, boolean wait) {
        this.loser = AccuratePlayer.create(minecraftVictim);
        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;

        boolean attackerNull = minecraftAttacker == null;

        Player victim = PlayerUtil.getFactoryPlayer(minecraftVictim);
        Player attacker = null;

        if (!attackerNull) {
            this.winner = AccuratePlayer.create(minecraftAttacker);
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
        if(victimData.gameOptions == null || victimData.gameOptions.duelsGame == null || victimData.gameOptions.duelsGame.isEnding) return;

        victim.destroyVanishingCursedItems();
        victim.inventory.dropAll();

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim);

        if(attacker != null){
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if((victimData.inDuel && attackerData.inDuel) && victimData.gameOptions.duelsGame == attackerData.gameOptions.duelsGame){
                this.endGame(victim, attacker, true);
            }
            return;
        }
        if(victimData.gameOptions.duelPlayer != null) {
            AccuratePlayer accurateAttacker = victimData.gameOptions.duelPlayer;
            attacker = accurateAttacker.get();
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if ((victimData.inDuel && attackerData.inDuel) && accurateAttacker.equals(attackerData.gameOptions.duelPlayer)) {
                this.endGame(victim, attacker, true);
            }
            return;
        }

        if(victimData.inDuel) {
            this.endGame(victim, null, false);
        }
    }
}