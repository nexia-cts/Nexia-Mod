package com.nexia.minigames.games.duels.gamemodes;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.DuelsSpawn;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.UUID;

public class GamemodeHandler {

    public static DuelGameMode identifyGamemode(String gameMode){
        if(gameMode.equalsIgnoreCase("axe")){
            return DuelGameMode.AXE;
        }

        if(gameMode.equalsIgnoreCase("bow_only")){
            return DuelGameMode.BOW_ONLY;
        }

        if(gameMode.equalsIgnoreCase("vanilla")){
            return DuelGameMode.VANILLA;
        }

        if(gameMode.equalsIgnoreCase("sword_only")){
            return DuelGameMode.SWORD_ONLY;
        }

        if(gameMode.equalsIgnoreCase("ffa")){
            return DuelGameMode.FFA;
        }

        if(gameMode.equalsIgnoreCase("hoe_only")){
            return DuelGameMode.HOE_ONLY;
        }

        if(gameMode.equalsIgnoreCase("uhc")){
            return DuelGameMode.UHC;
        }

        if(gameMode.equalsIgnoreCase("trident_only")){
            return DuelGameMode.TRIDENT_ONLY;
        }

        if(gameMode.equalsIgnoreCase("lobby") || gameMode.equalsIgnoreCase("leave")){
            return DuelGameMode.LOBBY;
        }
        return null;
    }

    public static void joinQueue(ServerPlayer player, String stringGameMode, boolean silent){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);

        if(gameMode == null){
            if(!silent){
                player.sendMessage(ChatFormat.formatFail("Invalid gamemode!"), Util.NIL_UUID);
            }
            return;
        }

        if(gameMode == DuelGameMode.LOBBY){
            LobbyUtil.sendGame(player, "duels", false);
            return;
        }

        if(!silent){
            player.sendMessage(ChatFormat.format("{b1}You have queued up for {b2}{}{b1}.", stringGameMode.toUpperCase()), Util.NIL_UUID);
        }

        removeQueue(player, stringGameMode, true);


        if(gameMode == DuelGameMode.AXE){
            DuelGameMode.AXE_QUEUE.add(player);
            if(DuelGameMode.AXE_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.AXE_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.SWORD_ONLY){
            DuelGameMode.SWORD_ONLY_QUEUE.add(player);
            if(DuelGameMode.SWORD_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.SWORD_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.TRIDENT_ONLY){
            DuelGameMode.TRIDENT_ONLY_QUEUE.add(player);
            if(DuelGameMode.TRIDENT_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.TRIDENT_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.HOE_ONLY){
            DuelGameMode.HOE_ONLY_QUEUE.add(player);
            if(DuelGameMode.HOE_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.HOE_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.BOW_ONLY){
            DuelGameMode.BOW_ONLY_QUEUE.add(player);
            if(DuelGameMode.BOW_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.BOW_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.VANILLA){
            DuelGameMode.VANILLA_QUEUE.add(player);
            if(DuelGameMode.VANILLA_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.VANILLA_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.UHC){
            DuelGameMode.UHC_QUEUE.add(player);
            if(DuelGameMode.UHC_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.UHC_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.FFA){
            DuelGameMode.FFA_QUEUE.add(player);
            if(DuelGameMode.FFA_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.FFA_QUEUE.get(0), stringGameMode, null,false);
            }
        }
    }

    public static void removeQueue(ServerPlayer player, String stringGameMode, boolean silent){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            if(!silent){
                player.sendMessage(ChatFormat.formatFail("Invalid gamemode!"), Util.NIL_UUID);
            }
            return;
        }

        if(!silent){
            player.sendMessage(ChatFormat.format("{b1}You have left the queue for {b2}{}{b1}.", stringGameMode.toUpperCase()), Util.NIL_UUID);
        }

        if(gameMode == DuelGameMode.AXE){
            DuelGameMode.AXE_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.BOW_ONLY) {
            DuelGameMode.BOW_ONLY_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.VANILLA) {
            DuelGameMode.VANILLA_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.SWORD_ONLY){
            DuelGameMode.SWORD_ONLY_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.FFA){
            DuelGameMode.FFA_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.UHC){
            DuelGameMode.UHC_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.TRIDENT_ONLY){
            DuelGameMode.TRIDENT_ONLY_QUEUE.remove(player);
        }

        if(gameMode == DuelGameMode.HOE_ONLY){
            DuelGameMode.HOE_ONLY_QUEUE.remove(player);
        }

    }

    public static void challengePlayer(ServerPlayer executor, ServerPlayer player, String stringGameMode, @Nullable String selectedmap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            executor.sendMessage(ChatFormat.formatFail("Invalid gamemode!"), Util.NIL_UUID);
            return;
        }
        if(executor == player) {
            executor.sendMessage(ChatFormat.formatFail("You cannot duel yourself!"), Util.NIL_UUID);
            return;
        }

        PlayerData executorData = PlayerDataManager.get(executor);
        PlayerData playerData = PlayerDataManager.get(player);

        if(executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(ChatFormat.formatFail("The player is currently dueling someone."), Util.NIL_UUID);
            return;
        }

        String map = selectedmap;
        if(map == null) {
            map = com.nexia.minigames.Main.config.duelsMaps.get(RandomUtil.randomInt(0, com.nexia.minigames.Main.config.duelsMaps.size()-1));
        } else {
            map = selectedmap;
            if (!com.nexia.minigames.Main.config.duelsMaps.contains(map.toLowerCase())) {
                executor.sendMessage(ChatFormat.formatFail("Invalid map!"), Util.NIL_UUID);
                return;
            }
        }

        executorData.inviteMap = map;
        executorData.inviting = true;
        executorData.invitingPlayer = player;

        if(playerData.inviting && playerData.invitingPlayer != null && playerData.invitingPlayer == executor && executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap)){
            GamemodeHandler.joinGamemode(executor, player, stringGameMode, map, true);
        } else if(!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(executor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){
            player.sendMessage(ChatFormat.format("{b1}Dueled {b2}{} {b1}on map {b2} {b1}with kit {}", player.getScoreboardName(), map, stringGameMode), Util.NIL_UUID);
            TextComponent message = new TextComponent(ChatFormat.brandColor2 + executor.getScoreboardName() + ChatFormat.brandColor1 + " has challenged you to a duel!");

            TextComponent kit = new TextComponent(ChatFormat.brandColor2 + "Kit: " + ChatFormat.brandColor1 + stringGameMode.toUpperCase());
            TextComponent mapName = new TextComponent(ChatFormat.brandColor2 + "Map: " + ChatFormat.brandColor1 + map.toUpperCase());

            TextComponent yes = new TextComponent("§8[§aACCEPT§8]  ");
            yes.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel " + executor.getScoreboardName() + " " + stringGameMode + " " + selectedmap)));

            TextComponent no = new TextComponent("§8[§cIGNORE§8]");

            TextComponent option = new TextComponent("");

            option.append(yes);
            option.append(no);

            player.sendMessage(message, Util.NIL_UUID);
            player.sendMessage(kit, Util.NIL_UUID);
            player.sendMessage(mapName, Util.NIL_UUID);
            player.sendMessage(option, Util.NIL_UUID);
        }
    }
    public static void joinGamemode(ServerPlayer invitor, ServerPlayer player, String stringGameMode, @Nullable String selectedmap, boolean silent){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            if(!silent){
                invitor.sendMessage(ChatFormat.formatFail("Invalid gamemode!"), Util.NIL_UUID);
            }
            return;
        }

        ServerLevel duelLevel = createWorld();
        String map = selectedmap;
        if(map == null){
            map = com.nexia.minigames.Main.config.duelsMaps.get(RandomUtil.randomInt(0, com.nexia.minigames.Main.config.duelsMaps.size()-1));
        }
        String name = duelLevel.dimension().toString().replaceAll("]", "").split(":")[2];

        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in duels:" + name + " run forceload add 0 0");
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in duels:" + name + " run " + DuelsGame.returnCommandMap(map));
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in duels:" + name + " run setblock 1 80 0 minecraft:redstone_block");

        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in duels:" + name + " if block 0 80 0 minecraft:structure_block run setblock 0 80 0 air");
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in duels:" + name + " if block 1 80 0 minecraft:redstone_block run setblock 1 80 0 air");


        PlayerData invitorData = PlayerDataManager.get(invitor);
        PlayerData playerData = PlayerDataManager.get(player);

        player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

        invitor.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        invitor.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

        PlayerUtil.resetHealthStatus(player);
        PlayerUtil.resetHealthStatus(invitor);

        player.teleportTo(duelLevel, 30, 85, 0, 0, 0);
        EntityPos playerPos = new EntityPos(0, 85, 0, 0, 0);
        player.setRespawnPosition(duelLevel.dimension(), playerPos.toBlockPos(), playerPos.yaw, true, false);
        playerData.inviting = false;
        playerData.invitingPlayer = null;
        playerData.inDuel = true;
        playerData.duelPlayer = invitor;

        invitor.teleportTo(duelLevel, -30, 85, 0, 0, 0);
        EntityPos invitorPos = new EntityPos(0, 85, 0, 0, 0);
        invitor.setRespawnPosition(duelLevel.dimension(), invitorPos.toBlockPos(), invitorPos.yaw, true, false);
        invitorData.inviting = false;
        invitorData.invitingPlayer = null;
        invitorData.inDuel = true;
        invitorData.duelPlayer = player;

        player.setGameMode(GameType.SURVIVAL);
        invitor.setGameMode(GameType.SURVIVAL);

        removeQueue(player, stringGameMode, true);
        removeQueue(invitor, stringGameMode, true);

        /*
        InventoryUtil.setInventory(player, stringGameMode.toLowerCase(), "/duels", true);
        InventoryUtil.setInventory(invitor, stringGameMode.toLowerCase(), "/duels", true);
         */

        player.sendMessage(ChatFormat.format("{b1}Your opponent: {b2}{}", invitor.getScoreboardName()), Util.NIL_UUID);
        invitor.sendMessage(ChatFormat.format("{b1}Your opponent: {b2}{}", player.getScoreboardName()), Util.NIL_UUID);

        ServerTime.minecraftServer.getCommands().performCommand(ServerTime.minecraftServer.createCommandSourceStack(), "/execute as " + player.getScoreboardName() + " run loadinventory " + stringGameMode.toLowerCase() + " " + player.getScoreboardName());
        ServerTime.minecraftServer.getCommands().performCommand(ServerTime.minecraftServer.createCommandSourceStack(), "/execute as " + invitor.getScoreboardName() + " run loadinventory " + stringGameMode.toLowerCase() + " " + invitor.getScoreboardName());

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;
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

        return ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", UUID.randomUUID().toString())).location(), config).asWorld();
    }

    public static void deleteWorld(String id) {
        ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("duels", id), null).delete();
    }

}
