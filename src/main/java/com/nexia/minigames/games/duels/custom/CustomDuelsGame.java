package com.nexia.minigames.games.duels.custom;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.InventoryUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class CustomDuelsGame { //implements Runnable{
    public NexiaPlayer p1;

    public UUID uuid;
    public NexiaPlayer p2;

    public String kitID;

    public DuelsMap map;

    public boolean isEnding = false;

    public boolean hasStarted = false;

    public int startTime;

    private int currentStartTime = 5;

    public int endTime;

    private int currentEndTime = 0;

    public ServerLevel level;

    public ArrayList<NexiaPlayer> spectators = new ArrayList<>();

    // Winner thingie

    public NexiaPlayer winner = null;

    public NexiaPlayer loser = null;

    private boolean shouldWait = false;

    public String perCustomKitID;

    public boolean perCustomDuel;

    public CustomDuelsGame(NexiaPlayer p1, NexiaPlayer p2, String kitID, DuelsMap map, ServerLevel level, int endTime, int startTime){
        this.p1 = p1;
        this.p2 = p2;
        this.kitID = kitID;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;
    }

    public CustomDuelsGame(NexiaPlayer p1, NexiaPlayer p2, String perCustomKitID, String perCustomKitID2, DuelsMap map, ServerLevel level, int endTime, int startTime){
        this.p1 = p1;
        this.p2 = p2;
        this.map = map;
        this.endTime = endTime;
        this.startTime = startTime;
        this.level = level;

        this.kitID = perCustomKitID;
        this.perCustomKitID = perCustomKitID2;

        this.perCustomDuel = true;
    }

    public static CustomDuelsGame startGame(NexiaPlayer p1, NexiaPlayer p2, String kitID, @Nullable DuelsMap selectedMap){

        String perCustomKitID = null;

        if(!DuelGameHandler.validCustomKit(p1, kitID)){
            NexiaCore.logger.error(String.format("[Nexia]: Invalid custom duel kit (%s) selected!", kitID));
            kitID = "";
        }

        DuelsPlayerData invitorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(p1);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(p2);

        if(invitorData.inviteOptions.perCustomDuel && !DuelGameHandler.validCustomKit(p1, invitorData.inviteOptions.inviteKit2)) {
            NexiaCore.logger.error(String.format("[Nexia]: Invalid per-custom (2) duel kit (%s) selected!", invitorData.inviteOptions.inviteKit2));
        } else {
            perCustomKitID = invitorData.inviteOptions.inviteKit2;
        }

        if(invitorData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(p1, invitorData.duelOptions.spectatingPlayer, false);
        }
        if(playerData.duelOptions.spectatingPlayer != null) {
            GamemodeHandler.unspectatePlayer(p2, playerData.duelOptions.spectatingPlayer, false);
        }

        UUID gameUUID = UUID.randomUUID();

        ServerLevel duelLevel = DuelGameHandler.createWorld(gameUUID.toString(), true);
        if(selectedMap == null){
            selectedMap = DuelsMap.duelsMaps.get(RandomUtil.randomInt(0, DuelsMap.duelsMaps.size()));
        }

        selectedMap.structureMap.pasteMap(duelLevel);

        selectedMap.p1Pos.teleportPlayer(duelLevel, p1);
        selectedMap.p2Pos.teleportPlayer(duelLevel, p2);

        p1.reset(true, Minecraft.GameMode.ADVENTURE);
        p2.reset(true, Minecraft.GameMode.ADVENTURE);

        playerData.inviteOptions.reset();
        playerData.inDuel = true;
        playerData.duelOptions.spectatingPlayer = null;

        invitorData.inDuel = true;
        invitorData.duelOptions.spectatingPlayer = null;

        removeQueue(p1, null, true);
        removeQueue(p2, null, true);


        p2.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p1.getRawName()).color(ChatFormat.brandColor2))));

        p1.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p2.getRawName())).color(ChatFormat.brandColor2)));

        File p1File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + p1.getUUID(), kitID.toLowerCase() + ".txt");
        if(p1File.exists()) {
            InventoryUtil.loadInventory(p1, "duels/custom/" + p1.getUUID(), kitID.toLowerCase());

            if(perCustomKitID != null && !perCustomKitID.trim().isEmpty()) {
                File p2File = new File(InventoryUtil.dirpath + File.separator + "duels" + File.separator + "custom" + File.separator + p2.getUUID(), perCustomKitID.toLowerCase() + ".txt");
                if(p2File.exists()) {
                    InventoryUtil.loadInventory(p2, "duels/custom/" + p2.getUUID(), perCustomKitID.toLowerCase());
                } else {
                    InventoryUtil.loadInventory(p2, "duels/custom/" + p1.getUUID(), kitID.toLowerCase());
                }

            } else {
                InventoryUtil.loadInventory(p2, "duels/custom/" + p1.getUUID(), kitID.toLowerCase());
            }


        }
        else {
            InventoryUtil.loadInventory(p1, "duels", "classic");
            InventoryUtil.loadInventory(p2, "duels", "classic");
        }

        invitorData.inviteOptions.reset();

        playerData.gameMode = DuelGameMode.CLASSIC;
        invitorData.gameMode = DuelGameMode.CLASSIC;

        CustomDuelsGame game;

        if(perCustomKitID != null) game = new CustomDuelsGame(p1, p2, kitID, perCustomKitID, selectedMap, duelLevel, 5, 5);
        else game = new CustomDuelsGame(p1, p2, kitID, selectedMap, duelLevel, 5, 5);

        playerData.gameOptions = new DuelOptions.GameOptions(game, p1);
        invitorData.gameOptions = new DuelOptions.GameOptions(game, p2);

        DuelGameHandler.customDuelsGames.add(game);

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
                NexiaPlayer attacker = this.winner;
                NexiaPlayer victim = this.loser;

                DuelsPlayerData victimData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(victim);
                DuelsPlayerData attackerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(attacker);

                for(NexiaPlayer spectator : this.spectators) {
                    spectator.runCommand("/hub", 0, false);
                }

                victimData.gameOptions = null;
                victimData.inDuel = false;
                removeQueue(victim, null, true);
                victimData.gameMode = DuelGameMode.LOBBY;
                victimData.inviteOptions.reset();
                victimData.duelOptions.spectatingPlayer = null;

                attackerData.gameOptions = null;
                attackerData.inDuel = false;
                removeQueue(attacker, null, true);
                attackerData.gameMode = DuelGameMode.LOBBY;
                attackerData.inviteOptions.reset();
                attackerData.duelOptions.spectatingPlayer = null;

                attackerData.savedData.incrementInteger("wins");
                victimData.savedData.incrementInteger("losses");

                this.isEnding = false;

                if(attacker.unwrap() != null) {
                    victim.runCommand("/hub", 0, false);
                }

                if(attacker.unwrap() != null) {
                    attacker.runCommand("/hub", 0, false);
                }

                for(ServerPlayer spectator : this.level.players()) {
                    new NexiaPlayer(spectator).runCommand("/hub", 0, false);
                    spectator.kill();
                }

                DuelGameHandler.deleteWorld(String.valueOf(this.uuid));
                DuelGameHandler.customDuelsGames.remove(this);
                return;
            }
        }
        if(!this.hasStarted) {

            this.currentStartTime--;

            this.map.p1Pos.teleportPlayer(this.level, p1);
            this.map.p2Pos.teleportPlayer(this.level, p2);

            if (this.startTime - this.currentStartTime >= this.startTime) {
                p1.sendSound(new EntityPos(p1.unwrap()), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                p2.sendSound(new EntityPos(p2.unwrap()), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 10, 2);
                p1.setGameMode(Minecraft.GameMode.SURVIVAL);
                p1.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                p1.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

                p2.setGameMode(Minecraft.GameMode.SURVIVAL);
                p2.removeTag(LobbyUtil.NO_DAMAGE_TAG);
                p2.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
                this.hasStarted = true;
                return;
            }

            Title title = DuelGameHandler.getTitle(this.currentStartTime);

            p1.sendTitle(title);
            p2.sendTitle(title);

            p1.sendSound(new EntityPos(p1.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
            p2.sendSound(new EntityPos(p2.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);

        }
    }

    public void endGame(@NotNull NexiaPlayer victim, @Nullable NexiaPlayer attacker, boolean wait) {
        this.loser = victim;
        this.shouldWait = wait;
        this.hasStarted = true;
        this.isEnding = true;

        boolean attackerNull = attacker == null || attacker.unwrap() == null;

        if (!attackerNull) {
            this.winner = attacker;
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

    public void death(@NotNull NexiaPlayer victim, @Nullable DamageSource source){
        DuelsPlayerData victimData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(victim);
        if(victimData.gameOptions == null || victimData.gameOptions.customDuelsGame == null || victimData.gameOptions.customDuelsGame.isEnding) return;

        victim.unwrap().destroyVanishingCursedItems();
        victim.unwrap().inventory.dropAll();

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim.unwrap());

        if(attacker != null){
            NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);
            DuelsPlayerData attackerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaAttacker);

            if((victimData.inDuel && attackerData.inDuel) && victimData.gameOptions.customDuelsGame == attackerData.gameOptions.customDuelsGame){
                this.endGame(victim, nexiaAttacker, true);
            }
            return;
        }
        if(victimData.gameOptions.duelPlayer != null) {
            NexiaPlayer duelAttacker = victimData.gameOptions.duelPlayer;
            DuelsPlayerData attackerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(duelAttacker);

            if ((victimData.inDuel && attackerData.inDuel) && duelAttacker.equals(victimData.gameOptions.duelPlayer)) {
                this.endGame(victim, duelAttacker, true);
            }
            return;
        }

        if(victimData.inDuel) {
            this.endGame(victim, null, false);
        }
    }
}