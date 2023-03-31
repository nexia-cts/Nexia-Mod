package com.nexia.minigames.games.duels;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.NxFileUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelsGame { //implements Runnable{

    public static final String duelsDirectory = NxFileUtil.addConfigDir("duels");

    public static void leave(ServerPlayer player){
        if(player.getLastDamageSource() != null && player.getLastDamageSource().getEntity() instanceof ServerPlayer){
            DuelsGame.death(player, player.getLastDamageSource());
            return;
        }
        PlayerData data = PlayerDataManager.get(player);
        data.inviting = false;
        data.inDuel = false;
        data.duelPlayer = null;
        removeQueue(player, data.gameMode.id, true);
        data.gameMode = DuelGameMode.LOBBY;
    }

    public static void starting(){
        DuelGameMode.AXE_QUEUE.clear();
        DuelGameMode.SWORD_ONLY_QUEUE.clear();
        DuelGameMode.FFA_QUEUE.clear();
        DuelGameMode.TRIDENT_ONLY_QUEUE.clear();
        DuelGameMode.HOE_ONLY_QUEUE.clear();
        DuelGameMode.UHC_QUEUE.clear();
        DuelGameMode.BOW_ONLY_QUEUE.clear();
        DuelGameMode.VANILLA_QUEUE.clear();
        DuelGameMode.SHIELD_QUEUE.clear();
        DuelGameMode.POT_QUEUE.clear();
        DuelGameMode.NETH_POT_QUEUE.clear();
        DuelGameMode.OG_VANILLA_QUEUE.clear();
        DuelGameMode.SMP_QUEUE.clear();

        for(ServerLevel level : ServerTime.minecraftServer.getAllLevels()){
            String[] split = level.dimension().toString().replaceAll("]", "").split(":");
            if(split[1].toLowerCase().contains("duels") && !split[2].toLowerCase().contains("hub")){
                GamemodeHandler.deleteWorld(split[2]);
            }
        }
    }

    public static String returnCommandMap(String mapname){
        int[] pos = new int[]{0, 0, 0};
        if(mapname.equalsIgnoreCase("desert")){
            pos[0] = -80;
            pos[1] = -45;
            pos[2] = -80;
        } else if(mapname.equalsIgnoreCase("city")){
            pos[0] = -45;
            pos[1] = -13;
            pos[2] = -30;
        } else if(mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
            pos[0] = -35;
            pos[1] = -3;
            pos[2] = -50;
        } else if(mapname.equalsIgnoreCase("plains")) {
            pos[0] = -30;
            pos[1] = -20;
            pos[2] = -39;
        } else if(mapname.equalsIgnoreCase("sky")){
            pos[0] = -33;
            pos[1] = -6;
            pos[2] = -19;
        }

        return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + mapname.toLowerCase() + "'" + ",posX:" + pos[0] + ",posY:" + pos[1] + ",posZ:" + pos[2] + "}";
    }

    public static void death(ServerPlayer victim, DamageSource source){
        if((source != null && source.getEntity() instanceof ServerPlayer attacker) && (victim != null)){
            PlayerData victimData = PlayerDataManager.get(victim);
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                ServerLevel duelLevel = attacker.getLevel();

                victimData.inviting = false;
                victimData.inDuel = false;
                victimData.duelPlayer = null;
                removeQueue(victim, victimData.gameMode.id, true);
                victimData.gameMode = DuelGameMode.LOBBY;

                attackerData.inviting = false;
                attackerData.inDuel = false;
                attackerData.duelPlayer = null;
                attackerData.gameMode = DuelGameMode.LOBBY;

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

                GamemodeHandler.deleteWorld(duelLevel.dimension().toString().replaceAll("]", "").split(":")[2]);
            }
        }
        if(((source == null) || (!(source.getEntity() instanceof ServerPlayer))) && (victim != null && PlayerDataManager.get(victim).duelPlayer != null)) {
            PlayerData victimData = PlayerDataManager.get(victim);
            ServerPlayer attacker = victimData.duelPlayer;
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if ((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                ServerLevel duelLevel = attacker.getLevel();

                victimData.inviting = false;
                victimData.inDuel = false;
                victimData.duelPlayer = null;
                removeQueue(victim, victimData.gameMode.id, true);
                victimData.gameMode = DuelGameMode.LOBBY;

                attackerData.inviting = false;
                attackerData.inDuel = false;
                attackerData.duelPlayer = null;
                attackerData.gameMode = DuelGameMode.LOBBY;

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

                GamemodeHandler.deleteWorld(duelLevel.dimension().toString().replaceAll("]", "").split(":")[2]);
            }
        }
    }
}
