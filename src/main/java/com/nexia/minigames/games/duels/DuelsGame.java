package com.nexia.minigames.games.duels;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelsGame { //implements Runnable{

    public ServerPlayer p1;

    public ServerPlayer p2;

    public DuelGameMode gameMode;

    public String selectedMap;

    public ServerLevel level;

    public DuelsGame(ServerPlayer p1, ServerPlayer p2, DuelGameMode gameMode, String selectedMap, ServerLevel level){
        this.p1 = p1;
        this.p2 = p2;
        this.gameMode = gameMode;
        this.selectedMap = selectedMap;
        this.level = level;
    }

    public static DuelsGame startGame(ServerPlayer p1, ServerPlayer p2, String stringGameMode, @Nullable String selectedMap){

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.FFA; // fallback gamemode incase somehow
            System.out.printf("[ERROR] Nexia: Invalid duel gamemode ({0}) selected! Using fallback one.%n", stringGameMode);
        }

        ServerLevel duelLevel = DuelGameHandler.createWorld();
        if(selectedMap == null){
            selectedMap = com.nexia.minigames.Main.config.duelsMaps.get(RandomUtil.randomInt(0, com.nexia.minigames.Main.config.duelsMaps.size()-1));
        }
        String name = duelLevel.dimension().toString().replaceAll("]", "").split(":")[2];

        String mapid = "duels";

        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " run forceload add 0 0");
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " run " + DuelGameHandler.returnCommandMap(selectedMap));
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " run setblock 1 80 0 minecraft:redstone_block");

        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " if block 0 80 0 minecraft:structure_block run setblock 0 80 0 air");
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " if block 1 80 0 minecraft:redstone_block run setblock 1 80 0 air");


        PlayerData invitorData = PlayerDataManager.get(p1);
        PlayerData playerData = PlayerDataManager.get(p2);

        p1.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        p1.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

        p2.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        p2.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

        PlayerUtil.resetHealthStatus(p1);
        PlayerUtil.resetHealthStatus(p2);

        float[] invitorpos = DuelGameHandler.returnPosMap(selectedMap, true);
        float[] playerpos = DuelGameHandler.returnPosMap(selectedMap, false);

        p1.teleportTo(duelLevel, playerpos[0], playerpos[1], playerpos[2], playerpos[3], playerpos[4]);
        EntityPos playerPos = new EntityPos(0, 85, 0, 0, 0);
        p1.setRespawnPosition(duelLevel.dimension(), playerPos.toBlockPos(), playerPos.yaw, true, false);
        playerData.inviting = false;
        playerData.invitingPlayer = null;
        playerData.inDuel = true;
        playerData.duelPlayer = p1;

        p2.teleportTo(duelLevel, invitorpos[0], invitorpos[1], invitorpos[2], invitorpos[3], invitorpos[4]);
        EntityPos invitorPos = new EntityPos(0, 85, 0, 0, 0);
        p2.setRespawnPosition(duelLevel.dimension(), invitorPos.toBlockPos(), invitorPos.yaw, true, false);
        invitorData.inviting = false;
        invitorData.invitingPlayer = null;
        invitorData.inDuel = true;
        invitorData.duelPlayer = p2;


        p1.setGameMode(GameType.SURVIVAL);
        p2.setGameMode(GameType.SURVIVAL);

        removeQueue(p1, null, true);
        removeQueue(p2, null, true);

        /*
        InventoryUtil.setInventory(player, stringGameMode.toLowerCase(), "/duels", true);
        InventoryUtil.setInventory(invitor, stringGameMode.toLowerCase(), "/duels", true);
         */

        p1.sendMessage(ChatFormat.format("{b1}Your opponent: {b2}{}", p2.getScoreboardName()), Util.NIL_UUID);
        p2.sendMessage(ChatFormat.format("{b1}Your opponent: {b2}{}", p1.getScoreboardName()), Util.NIL_UUID);

        ServerTime.minecraftServer.getCommands().performCommand(ServerTime.minecraftServer.createCommandSourceStack(), "/execute as " + p1.getScoreboardName() + " run loadinventory " + stringGameMode.toLowerCase() + " " + p1.getScoreboardName());
        ServerTime.minecraftServer.getCommands().performCommand(ServerTime.minecraftServer.createCommandSourceStack(), "/execute as " + p2.getScoreboardName() + " run loadinventory " + stringGameMode.toLowerCase() + " " + p2.getScoreboardName());

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;

        DuelsGame game = new DuelsGame(p1, p2, gameMode, selectedMap, duelLevel);

        invitorData.duelsGame = game;
        playerData.duelsGame = game;

        return game;
    }


    public static void endGame(@NotNull ServerPlayer victim, @NotNull ServerPlayer attacker, boolean wait) {
        PlayerData victimData = PlayerDataManager.get(victim);
        PlayerData attackerData = PlayerDataManager.get(attacker);

        ServerLevel duelLevel = attacker.getLevel();

        victimData.inviting = false;
        victimData.inDuel = false;
        victimData.duelPlayer = null;
        victimData.inviteMap = "";
        victimData.inviteKit = "";
        removeQueue(victim, victimData.gameMode.id, true);
        victimData.gameMode = DuelGameMode.LOBBY;
        victimData.duelsGame = null;

        attackerData.inviting = false;
        attackerData.inDuel = false;
        attackerData.duelPlayer = null;
        attackerData.inviteKit = "";
        attackerData.inviteMap = "";
        attackerData.gameMode = DuelGameMode.LOBBY;
        attackerData.duelsGame = null;

        attackerData.savedData.wins++;
        victimData.savedData.loss++;

        victim.setGameMode(GameType.SPECTATOR);
        victim.teleportTo(attacker.getX(), attacker.getY(), attacker.getZ());

        attacker.sendMessage(ChatFormat.format("{b2}{} {b1}has won the duel!", attacker.getScoreboardName()), Util.NIL_UUID);
        victim.sendMessage(ChatFormat.format("{b2}{} {b1}has won the duel!", attacker.getScoreboardName()), Util.NIL_UUID);

        attacker.teleportTo(DuelsSpawn.duelWorld, DuelsSpawn.spawn.x, DuelsSpawn.spawn.y, DuelsSpawn.spawn.z, DuelsSpawn.spawn.yaw, DuelsSpawn.spawn.pitch);
        attacker.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);

        victim.teleportTo(DuelsSpawn.duelWorld, DuelsSpawn.spawn.x, DuelsSpawn.spawn.y, DuelsSpawn.spawn.z, DuelsSpawn.spawn.yaw, DuelsSpawn.spawn.pitch);
        victim.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);

        PlayerUtil.resetHealthStatus(attacker);
        PlayerUtil.resetHealthStatus(victim);

        // Fix command bug (/duel & /queue being red indicating you can't use them, but you actually still can)
        LobbyUtil.sendGame(victim, "duels", false);
        LobbyUtil.sendGame(attacker, "duels", false);

        attacker.inventory.clearContent();
        victim.inventory.clearContent();

        attacker.setGameMode(GameType.ADVENTURE);
        victim.setGameMode(GameType.ADVENTURE);

        DuelGameHandler.deleteWorld(duelLevel.dimension().toString().replaceAll("]", "").split(":")[2]);
    }

    public static void death(@NotNull ServerPlayer victim, @Nullable DamageSource source){
        if(source != null && source.getEntity() instanceof ServerPlayer attacker){
            PlayerData victimData = PlayerDataManager.get(victim);
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if((victimData.inDuel && attackerData.inDuel) && victimData.duelsGame == attackerData.duelsGame){
                DuelsGame.endGame(victim, attacker, true);
            }
        }
        if((source == null || !(source.getEntity() instanceof ServerPlayer)) && PlayerDataManager.get(victim).duelPlayer != null) {
            PlayerData victimData = PlayerDataManager.get(victim);
            ServerPlayer attacker = victimData.duelPlayer;
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if ((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                DuelsGame.endGame(victim, attacker, true);
            }
        }
    }
}
