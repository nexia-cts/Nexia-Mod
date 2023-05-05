package com.nexia.minigames.games.duels;

import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelGameHandler {
    public static void leave(ServerPlayer player) {
        if (player.getLastDamageSource() != null) {
            DuelsGame.death(player, null, player.getLastDamageSource());
            return;
        }
        PlayerData data = PlayerDataManager.get(player);
        data.inviting = false;
        data.inDuel = false;
        data.duelPlayer = null;
        removeQueue(player, data.gameMode.id, true);
        data.gameMode = DuelGameMode.LOBBY;
        data.duelsGame = null;
    }

    public static void starting() {
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
        DuelGameMode.UHC_SHIELD_QUEUE.clear();


        for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            String[] split = level.dimension().toString().replaceAll("]", "").split(":");
            if (split[1].toLowerCase().contains("duels") && !split[2].toLowerCase().contains("hub")) {
                DuelGameHandler.deleteWorld(split[2]);
            }
        }
    }

    public static float[] returnPosMap(String mapname, boolean player1){
        float[] pos = new float[]{0, 83, 0, 0, 0};

        if(player1){
            if (mapname.equalsIgnoreCase("desert")) {
                pos[0] = -30;
            } else if (mapname.equalsIgnoreCase("city")) {
                pos[0] = 36;
                pos[1] = 80;
                pos[3] = 90;
            } else if (mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
                pos[0] = 0;
                pos[1] = 80;
                pos[2] = -41;
            } else if (mapname.equalsIgnoreCase("plains")) {
                pos[0] = 0;
                pos[1] = 80;
                pos[2] = 25;
            } else if (mapname.equalsIgnoreCase("sky")) {
                pos[0] = 31;
                pos[2] = -2;
                pos[3] = 90;
            }
        } else {
            if (mapname.equalsIgnoreCase("desert")) {
                pos[0] = 30;
            } else if (mapname.equalsIgnoreCase("city")) {
                pos[0] = -35;
                pos[1] = 80;
                pos[3] = -90;
            } else if (mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
                pos[0] = 0;
                pos[1] = 80;
                pos[2] = 41;
            } else if (mapname.equalsIgnoreCase("plains")) {
                pos[0] = 0;
                pos[1] = 80;
                pos[2] = 25;
                pos[3] = 180;
            } else if (mapname.equalsIgnoreCase("sky")) {
                pos[0] = -31;
                pos[2] = 2;
                pos[3] = -90;
            }
        }

        return pos;
    }

    public static String returnCommandMap(String mapname) {
        int[] pos = new int[]{0, 0, 0};

        String rotation = "";
        if (mapname.equalsIgnoreCase("desert")) {
            pos[0] = -80;
            pos[1] = -45;
            pos[2] = -80;
        } else if (mapname.equalsIgnoreCase("city")) {
            pos[0] = -45;
            pos[1] = -13;
            pos[2] = -30;
        } else if (mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
            pos[0] = -35;
            pos[1] = -3;
            pos[2] = -50;
        } else if (mapname.equalsIgnoreCase("plains")) {
            pos[0] = 39;
            pos[1] = -20;
            pos[2] = -39;
            rotation = "CLOCKWISE_90";
        } else if (mapname.equalsIgnoreCase("sky")) {
            pos[0] = -33;
            pos[1] = -6;
            pos[2] = -19;
        }

        if(rotation.trim().length() != 0){
            return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + mapname.toLowerCase() + "'" + ",posX:" + pos[0] + ",posY:" + pos[1] + ",posZ:" + pos[2] + ",rotation:\"" + rotation + "\"}";
        } else {
            return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + mapname.toLowerCase() + "'" + ",posX:" + pos[0] + ",posY:" + pos[1] + ",posZ:" + pos[2] + "}";
        }
    }

    public static ServerLevel createWorld(){
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(DuelsSpawn.duelWorld.dimensionType())
                .setGenerator(DuelsSpawn.duelWorld.getChunkSource().getGenerator())
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                .setGameRule(GameRules.RULE_DAYLIGHT, false)
                .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0)
                .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
                .setTimeOfDay(6000);

        //return ServerTime.fantasy.openTemporaryWorld(config).asWorld();
        return ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", UUID.randomUUID().toString().replaceAll("-", ""))).location(), config).asWorld();
    }

    public static void deleteWorld(String id) {
        RuntimeWorldHandle worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", id)).location(), null);
        worldHandle.delete();
    }
}