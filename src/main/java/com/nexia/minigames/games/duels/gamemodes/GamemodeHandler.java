package com.nexia.minigames.games.duels.gamemodes;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

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

    public static void joinQueue(ServerPlayer minecraftPlayer, String stringGameMode, boolean silent){
        if(stringGameMode.equalsIgnoreCase("lobby") || stringGameMode.equalsIgnoreCase("leave")){
            LobbyUtil.sendGame(minecraftPlayer, "duels", false, false);
            return;
        }

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);

        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        if(gameMode == null){
            if(!silent){
                player.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            }
            return;
        }



        if(!silent){
            player.sendMessage(
                    Component.text("You have queued up for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                    .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                            .append(Component.text(".").decoration(ChatFormat.bold, false))
            );

        }

        removeQueue(minecraftPlayer, stringGameMode, true);


        if(gameMode == DuelGameMode.AXE){
            DuelGameMode.AXE_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.AXE_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.AXE_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.SWORD_ONLY){
            DuelGameMode.SWORD_ONLY_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.SWORD_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.SWORD_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.TRIDENT_ONLY){
            DuelGameMode.TRIDENT_ONLY_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.TRIDENT_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.TRIDENT_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.HOE_ONLY){
            DuelGameMode.HOE_ONLY_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.HOE_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.HOE_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.BOW_ONLY){
            DuelGameMode.BOW_ONLY_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.BOW_ONLY_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.BOW_ONLY_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.SHIELD){
            DuelGameMode.SHIELD_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.SHIELD_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.SHIELD_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.POT){
            DuelGameMode.POT_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.POT_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.POT_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.NETH_POT){
            DuelGameMode.NETH_POT_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.NETH_POT_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.NETH_POT_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.OG_VANILLA){
            DuelGameMode.OG_VANILLA_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.OG_VANILLA_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.OG_VANILLA_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.SMP){
            DuelGameMode.SMP_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.SMP_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.SMP_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.UHC_SHIELD){
            DuelGameMode.UHC_SHIELD_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.UHC_SHIELD_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.UHC_SHIELD_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.VANILLA){
            DuelGameMode.VANILLA_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.VANILLA_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.VANILLA_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.UHC){
            DuelGameMode.UHC_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.UHC_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.UHC_QUEUE.get(0), stringGameMode, null,false);
            }
        }

        if(gameMode == DuelGameMode.FFA){
            DuelGameMode.FFA_QUEUE.add(minecraftPlayer);
            if(DuelGameMode.FFA_QUEUE.size() >= 2){
                GamemodeHandler.joinGamemode(minecraftPlayer, DuelGameMode.FFA_QUEUE.get(0), stringGameMode, null,false);
            }
        }
    }

    public static void removeQueue(ServerPlayer minecraftPlayer, @Nullable String stringGameMode, boolean silent){
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        if(stringGameMode != null) {
            DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
            if(gameMode == null){
                if(!silent){
                    player.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
                }
                return;
            }
            if(gameMode == DuelGameMode.AXE){
                DuelGameMode.AXE_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.BOW_ONLY) {
                DuelGameMode.BOW_ONLY_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.SHIELD) {
                DuelGameMode.SHIELD_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.POT) {
                DuelGameMode.POT_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.NETH_POT) {
                DuelGameMode.NETH_POT_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.OG_VANILLA) {
                DuelGameMode.OG_VANILLA_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.SMP) {
                DuelGameMode.SMP_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.UHC_SHIELD) {
                DuelGameMode.UHC_SHIELD_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.VANILLA) {
                DuelGameMode.VANILLA_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.SWORD_ONLY){
                DuelGameMode.SWORD_ONLY_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.FFA){
                DuelGameMode.FFA_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.UHC){
                DuelGameMode.UHC_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.TRIDENT_ONLY){
                DuelGameMode.TRIDENT_ONLY_QUEUE.remove(minecraftPlayer);
            }

            if(gameMode == DuelGameMode.HOE_ONLY){
                DuelGameMode.HOE_ONLY_QUEUE.remove(minecraftPlayer);
            }
            if(!silent){
                player.sendMessage(
                        ChatFormat.nexiaMessage()
                                        .append(Component.text("You have left the queue for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                                                .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                ));
            }
        } else {
            DuelGameMode.AXE_QUEUE.remove(minecraftPlayer);
            DuelGameMode.BOW_ONLY_QUEUE.remove(minecraftPlayer);
            DuelGameMode.SHIELD_QUEUE.remove(minecraftPlayer);
            DuelGameMode.POT_QUEUE.remove(minecraftPlayer);
            DuelGameMode.NETH_POT_QUEUE.remove(minecraftPlayer);
            DuelGameMode.OG_VANILLA_QUEUE.remove(minecraftPlayer);
            DuelGameMode.SMP_QUEUE.remove(minecraftPlayer);
            DuelGameMode.UHC_SHIELD_QUEUE.remove(minecraftPlayer);
            DuelGameMode.VANILLA_QUEUE.remove(minecraftPlayer);
            DuelGameMode.SWORD_ONLY_QUEUE.remove(minecraftPlayer);
            DuelGameMode.FFA_QUEUE.remove(minecraftPlayer);
            DuelGameMode.UHC_QUEUE.remove(minecraftPlayer);
            DuelGameMode.TRIDENT_ONLY_QUEUE.remove(minecraftPlayer);
            DuelGameMode.HOE_ONLY_QUEUE.remove(minecraftPlayer);
        }


    }

    public static void joinGamemode(ServerPlayer invitor, ServerPlayer player, String stringGameMode, @Nullable String selectedmap, boolean silent){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            if(!silent){
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            }
            return;
        }
        DuelsGame.startGame(invitor, player, stringGameMode, selectedmap);
    }

    public static void challengePlayer(ServerPlayer minecraftExecutor, ServerPlayer minecraftPlayer, String stringGameMode, @Nullable String selectedmap){

        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            executor.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            return;
        }
        if(minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if(executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.").color(ChatFormat.failColor));
            return;
        }

        if(com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY){
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        String map = selectedmap;
        if(map == null) {
            map = com.nexia.minigames.Main.config.duelsMaps.get(RandomUtil.randomInt(0, com.nexia.minigames.Main.config.duelsMaps.size()-1));
        } else {
            map = selectedmap;
            if (!com.nexia.minigames.Main.config.duelsMaps.contains(map.toLowerCase())) {
                executor.sendMessage(Component.text("Invalid map!").color(ChatFormat.failColor));
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

        if(executorData.invitingPlayer != minecraftPlayer) {
            executorData.invitingPlayer = minecraftPlayer;
        }

        if(playerData.inviting && playerData.invitingPlayer != null && playerData.invitingPlayer == minecraftExecutor && executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) && executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)){
            GamemodeHandler.joinGamemode(minecraftExecutor, minecraftPlayer, stringGameMode, map, true);
        } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(minecraftExecutor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){
            executor.sendMessage(ChatFormat.nexiaMessage()
                    .append(Component.text("Sending a duel request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false)
                            .append(Component.text(" with kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(stringGameMode).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

            Component message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                            .append(Component.text(" has challenged you to a duel!").color(ChatFormat.normalColor)
            );


            Component kit = Component.text("Kit: ").color(ChatFormat.brandColor1)
                            .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.normalColor)
            );

            Component mapName = Component.text("Map: ").color(ChatFormat.brandColor1)
                    .append(Component.text(map.toUpperCase()).color(ChatFormat.normalColor)
            );

            Component yes = Component.text("[").color(NamedTextColor.DARK_GRAY)
                            .append(Component.text("ACCEPT")
                                    .color(ChatFormat.greenColor)
                                    .decorate(ChatFormat.bold)
                                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/duel " + executor.getRawName() + " " + stringGameMode + " " + map)))
                                    .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
            );

            Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                            .append(Component.text("IGNORE").color(ChatFormat.failColor).decoration(ChatFormat.bold, true))
                                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY)
            );


            player.sendMessage(message);
            player.sendMessage(kit);
            player.sendMessage(mapName);
            player.sendMessage(yes.append(no));
        }
    }
}