package com.nexia.minigames.games.duels.gamemodes;

import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
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
import net.minecraft.network.chat.HoverEvent;
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
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.UUID;

public class GamemodeHandler {

    public static DuelGameMode identifyGamemode(String gameMode){
        if(gameMode.equalsIgnoreCase("axe")){
            return DuelGameMode.AXE;
        }

        if(gameMode.equalsIgnoreCase("bow_only")){
            return DuelGameMode.BOW_ONLY;
        }

        if(gameMode.equalsIgnoreCase("shield")){
            return DuelGameMode.SHIELD;
        }

        if(gameMode.equalsIgnoreCase("pot")){
            return DuelGameMode.POT;
        }

        if(gameMode.equalsIgnoreCase("neth_pot")){
            return DuelGameMode.NETH_POT;
        }

        if(gameMode.equalsIgnoreCase("og_vanilla")){
            return DuelGameMode.OG_VANILLA;
        }

        if(gameMode.equalsIgnoreCase("uhc_shield")){
            return DuelGameMode.UHC_SHIELD;
        }


        if(gameMode.equalsIgnoreCase("vanilla")){
            return DuelGameMode.VANILLA;
        }

        if(gameMode.equalsIgnoreCase("smp")){
            return DuelGameMode.SMP;
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

        return null;
    }

    public static void joinQueue(ServerPlayer player, String stringGameMode, boolean silent){
        if(stringGameMode.equalsIgnoreCase("lobby") || stringGameMode.equalsIgnoreCase("leave")){
            LobbyUtil.sendGame(player, "duels", false);
            return;
        }

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);

        if(gameMode == null){
            if(!silent){
                player.sendMessage(ChatFormat.formatFail("Invalid gamemode!"), Util.NIL_UUID);
            }
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

        if(gameMode == DuelGameMode.SHIELD){
            DuelGameMode.SHIELD_QUEUE.add(player);
            if(DuelGameMode.SHIELD_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.SHIELD_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.POT){
            DuelGameMode.POT_QUEUE.add(player);
            if(DuelGameMode.POT_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.POT_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.NETH_POT){
            DuelGameMode.NETH_POT_QUEUE.add(player);
            if(DuelGameMode.NETH_POT_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.NETH_POT_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.OG_VANILLA){
            DuelGameMode.OG_VANILLA_QUEUE.add(player);
            if(DuelGameMode.OG_VANILLA_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.OG_VANILLA_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.SMP){
            DuelGameMode.SMP_QUEUE.add(player);
            if(DuelGameMode.SMP_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.SMP_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.UHC_SHIELD){
            DuelGameMode.UHC_SHIELD_QUEUE.add(player);
            if(DuelGameMode.UHC_SHIELD_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(player, DuelGameMode.UHC_SHIELD_QUEUE.get(0), stringGameMode, null,false);
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

    public static void removeQueue(ServerPlayer player, @Nullable String stringGameMode, boolean silent){
        if(stringGameMode != null) {
            DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
            if(gameMode == null){
                if(!silent){
                    player.sendMessage(ChatFormat.formatFail("Invalid gamemode!"), Util.NIL_UUID);
                }
                return;
            }
            if(gameMode == DuelGameMode.AXE){
                DuelGameMode.AXE_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.BOW_ONLY) {
                DuelGameMode.BOW_ONLY_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.SHIELD) {
                DuelGameMode.SHIELD_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.POT) {
                DuelGameMode.POT_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.NETH_POT) {
                DuelGameMode.NETH_POT_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.OG_VANILLA) {
                DuelGameMode.OG_VANILLA_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.SMP) {
                DuelGameMode.SMP_QUEUE.remove(player);
            }

            if(gameMode == DuelGameMode.UHC_SHIELD) {
                DuelGameMode.UHC_SHIELD_QUEUE.remove(player);
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
            if(!silent){
                player.sendMessage(ChatFormat.format("{b1}You have left the queue for {b2}{}{b1}.", stringGameMode.toUpperCase()), Util.NIL_UUID);
            }
        } else {
            DuelGameMode.AXE_QUEUE.remove(player);
            DuelGameMode.BOW_ONLY_QUEUE.remove(player);
            DuelGameMode.SHIELD_QUEUE.remove(player);
            DuelGameMode.POT_QUEUE.remove(player);
            DuelGameMode.NETH_POT_QUEUE.remove(player);
            DuelGameMode.OG_VANILLA_QUEUE.remove(player);
            DuelGameMode.SMP_QUEUE.remove(player);
            DuelGameMode.UHC_SHIELD_QUEUE.remove(player);
            DuelGameMode.VANILLA_QUEUE.remove(player);
            DuelGameMode.SWORD_ONLY_QUEUE.remove(player);
            DuelGameMode.FFA_QUEUE.remove(player);
            DuelGameMode.UHC_QUEUE.remove(player);
            DuelGameMode.TRIDENT_ONLY_QUEUE.remove(player);
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

        if(com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode != PlayerGameMode.DUELS){
            executor.sendMessage(ChatFormat.formatFail("That player is not in duels!"), Util.NIL_UUID);
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

        if(!executorData.inviteMap.equalsIgnoreCase(map)) {
            executorData.inviteMap = map;
        }

        if(!executorData.inviteKit.equalsIgnoreCase(stringGameMode.toUpperCase())) {
            executorData.inviteKit = stringGameMode.toUpperCase();
        }

        if(!executorData.inviting) {
            executorData.inviting = true;
        }

        if(executorData.invitingPlayer != player) {
            executorData.invitingPlayer = player;
        }

        if(playerData.inviting && playerData.invitingPlayer != null && playerData.invitingPlayer == executor && executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) && executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)){
            GamemodeHandler.joinGamemode(executor, player, stringGameMode, map, true);
        } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(executor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){
            executor.sendMessage(ChatFormat.format("{b1}Sending a duel request to {b2}{} {b1}on map {b2}{} {b1}with kit {b2}{}{b1}.", player.getScoreboardName(), map, stringGameMode), Util.NIL_UUID);
            TextComponent message = new TextComponent(ChatFormat.brandColor2 + executor.getScoreboardName() + ChatFormat.brandColor1 + " has challenged you to a duel!");

            TextComponent kit = new TextComponent(ChatFormat.brandColor2 + "Kit: " + ChatFormat.brandColor1 + stringGameMode.toUpperCase());
            TextComponent mapName = new TextComponent(ChatFormat.brandColor2 + "Map: " + ChatFormat.brandColor1 + map.toUpperCase());

            TextComponent yes = new TextComponent("§8[§aACCEPT§8]  ");
            String finalMap = map;
            yes.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel " + executor.getScoreboardName() + " " + stringGameMode + " " + finalMap)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cClick me"))));

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
        DuelsGame.startGame(invitor, player, stringGameMode, selectedmap);
    }
}
