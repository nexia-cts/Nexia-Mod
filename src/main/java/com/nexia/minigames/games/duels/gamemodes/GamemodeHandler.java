package com.nexia.minigames.games.duels.gamemodes;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GamemodeHandler {

    public static DuelGameMode identifyGamemode(@NotNull String gameMode) {
        for(DuelGameMode duelGameMode : DuelGameMode.duelGameModes) {
            if(duelGameMode.id.equalsIgnoreCase(gameMode)) return duelGameMode;
        }
        return null;
    }

    public static boolean isInQueue(@NotNull NexiaPlayer player, @NotNull DuelGameMode gameMode) {
        return gameMode.queue.contains(player);
    }

    public static void joinQueue(NexiaPlayer player, String stringGameMode, boolean silent) {
        if (stringGameMode.equalsIgnoreCase("leave")) {
            removeQueue(player, null, silent);
            return;
        }
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);

        if (gameMode == null) {
            if (!silent) player.sendNexiaMessage("Invalid gamemode!");
            return;
        }

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (data.duelOptions.duelsTeam != null) {
            if (!silent) player.sendNexiaMessage("You are in a team!");
            return;
        }

        if (!silent) {
            player.sendNexiaMessage(
                    Component.text("You have queued up for ", ChatFormat.normalColor)
                            .append(Component.text(stringGameMode.toUpperCase(), ChatFormat.brandColor2))
                            .append(Component.text("."))
            );

        }

        removeQueue(player, stringGameMode, true);

        gameMode.queue.add(player);
        if (gameMode.queue.size() >= 2) {
            GamemodeHandler.joinGamemode(player, gameMode.queue.getFirst(), stringGameMode, null, false);
        }
    }

    public static void removeQueue(NexiaPlayer player, @Nullable String stringGameMode, boolean silent) {
        if(stringGameMode == null || stringGameMode.trim().isEmpty()) {
            for(DuelGameMode gameMode : DuelGameMode.duelGameModes) {
                gameMode.queue.remove(player);
            }
            return;
        }

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            if (!silent) player.sendNexiaMessage("Invalid gamemode!");
            return;
        }

        gameMode.queue.remove(player);

        if (!silent) {
            player.sendNexiaMessage(
                    Component.text("You have left the queue for ", ChatFormat.normalColor)
                            .append(Component.text(stringGameMode.toUpperCase(), ChatFormat.brandColor2))
                            .append(Component.text(".", ChatFormat.normalColor))
            );
        }

    }


    public static void spectatePlayer(@NotNull NexiaPlayer executor, @NotNull NexiaPlayer player) {
        if (executor.equals(player)) {
            executor.sendNexiaMessage("You may not spectate yourself!");
            return;
        }

        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (playerData.gameOptions == null || (!playerData.inDuel && playerData.gameOptions.teamDuelsGame == null && playerData.gameOptions.duelsGame == null)) {
            executor.sendNexiaMessage("That player is not in a duel!");
            return;
        }


        DuelsPlayerData executorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);

        if (executorData.gameMode == DuelGameMode.SPECTATING) {
            unspectatePlayer(executor, player, false);
        }

        executor.setGameMode(Minecraft.GameMode.SPECTATOR);
        executor.teleport(player.getLocation());

        DuelsGame duelsGame = playerData.gameOptions.duelsGame;
        TeamDuelsGame teamDuelsGame = playerData.gameOptions.teamDuelsGame;

        Component spectateMSG = Component.text(String.format("(%s started spectating)", executor.getRawName()), ChatFormat.systemColor).decorate(ChatFormat.italic);

        if (teamDuelsGame != null) {
            teamDuelsGame.spectators.add(executor);
            List<NexiaPlayer> everyTeamMember = teamDuelsGame.team1.all;
            everyTeamMember.addAll(teamDuelsGame.team2.all);
            for (NexiaPlayer players : everyTeamMember) {
                players.sendMessage(spectateMSG);
            }
        } else if (duelsGame != null) {
            duelsGame.spectators.add(executor);
            duelsGame.p1.sendMessage(spectateMSG);
            duelsGame.p2.sendMessage(spectateMSG);
        }


        executor.sendNexiaMessage(
                Component.text("You are now spectating ", ChatFormat.normalColor)
                        .append(Component.text(player.getRawName(), ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
        );

        executorData.duelOptions.spectatingPlayer = player;
        executorData.gameMode = DuelGameMode.SPECTATING;
    }

    public static void unspectatePlayer(@NotNull NexiaPlayer executor, @Nullable NexiaPlayer player, boolean teleport) {
        DuelsPlayerData playerData = null;

        if (player != null && player.unwrap() != null) {
            playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);
        }

        DuelsGame duelsGame = null;
        TeamDuelsGame teamDuelsGame = null;


        if(playerData != null && playerData.inDuel && playerData.gameOptions != null) {
            if(playerData.gameOptions.duelsGame != null) duelsGame = playerData.gameOptions.duelsGame;
            else if(playerData.gameOptions.teamDuelsGame != null) teamDuelsGame = playerData.gameOptions.teamDuelsGame;
        }

        DuelsPlayerData executorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);
        Component spectateMSG = Component.text(String.format("(%s started spectating)", executor.getRawName()), ChatFormat.systemColor).decorate(ChatFormat.italic);

        if (duelsGame != null || teamDuelsGame != null) {
            executor.sendNexiaMessage(
                    Component.text("You have stopped spectating ", ChatFormat.normalColor)
                            .append(Component.text(player.getRawName(), ChatFormat.brandColor1))
            );
        }

        if (duelsGame != null) {
            duelsGame.spectators.remove(executor);

            duelsGame.p1.sendMessage(spectateMSG);
            duelsGame.p2.sendMessage(spectateMSG);
        } else if (teamDuelsGame != null) {
            teamDuelsGame.spectators.remove(executor);

            List<NexiaPlayer> everyTeamPlayer = teamDuelsGame.team1.all;
            everyTeamPlayer.addAll(teamDuelsGame.team2.all);

            for (NexiaPlayer players : everyTeamPlayer) {
                players.sendMessage(spectateMSG);
            }
        }


        executorData.gameMode = DuelGameMode.LOBBY;
        executorData.duelOptions.spectatingPlayer = null;

        LobbyUtil.returnToLobby(executor, teleport);

    }
    public static void joinGamemode(NexiaPlayer invitor, NexiaPlayer player, String stringGameMode, @Nullable DuelsMap selectedmap, boolean silent) {
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            if (!silent) {
                invitor.sendMessage(Component.text("Invalid gamemode!", ChatFormat.failColor));
            }
            return;
        }
        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(invitor);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (data.duelOptions.duelsTeam != null && data.duelOptions.duelsTeam.getPeople().contains(player)) {
            if (!silent) {
                invitor.sendMessage(Component.text("You cannot duel people on your team!", ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam != null && !data.duelOptions.duelsTeam.isLeader(invitor)) {
            if (!silent) {
                invitor.sendMessage(Component.text("You are not the team leader!", ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam == null && playerData.duelOptions.duelsTeam != null) {
            DuelsTeam.createTeam(invitor, true);
        } else if(data.duelOptions.duelsTeam != null && playerData.duelOptions.duelsTeam == null) {
            DuelsTeam.createTeam(player, true);
        }

        if (data.duelOptions.duelsTeam != null && playerData.duelOptions.duelsTeam != null) {
            TeamDuelsGame.startGame(data.duelOptions.duelsTeam, playerData.duelOptions.duelsTeam, stringGameMode, selectedmap);
            return;
        }

        DuelsGame.startGame(invitor, player, stringGameMode, selectedmap);

    }

    public static void joinCustomGamemode(NexiaPlayer invitor, NexiaPlayer player, String kitID, @Nullable DuelsMap selectedmap, boolean silent) {
        if (!DuelGameHandler.validCustomKit(invitor, kitID)) {
            if (!silent) {
                invitor.sendMessage(Component.text("Invalid custom kit!", ChatFormat.failColor));
            }
            return;
        }

        DuelsPlayerData data = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(invitor);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if(data.inviteOptions.inviteKit2 != null && !DuelGameHandler.validCustomKit(player, data.inviteOptions.inviteKit2)) {
            if (!silent) {
                invitor.sendMessage(Component.text("Invalid per-custom kit (2)!", ChatFormat.failColor));
            }
            data.inviteOptions.inviteKit2 = null;
            return;
        } else if(data.inviteOptions.inviteKit2 != null && DuelGameHandler.validCustomKit(player, data.inviteOptions.inviteKit2)){
            data.inviteOptions.perCustomDuel = true;
        }
        
        if (data.duelOptions.duelsTeam != null && data.duelOptions.duelsTeam.getPeople().contains(player)) {
            if (!silent) {
                invitor.sendMessage(Component.text("You cannot duel people on your team!", ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam != null && !data.duelOptions.duelsTeam.isLeader(invitor)) {
            if (!silent) {
                invitor.sendMessage(Component.text("You are not the team leader!", ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam == null && playerData.duelOptions.duelsTeam != null) {
            DuelsTeam.createTeam(invitor, false);
        }

        if (data.duelOptions.duelsTeam != null && playerData.duelOptions.duelsTeam != null) {
            CustomTeamDuelsGame.startGame(data.duelOptions.duelsTeam, playerData.duelOptions.duelsTeam, kitID, selectedmap);
            return;
        }

        CustomDuelsGame.startGame(invitor, player, kitID, selectedmap);

    }

    public static void acceptDuel(@NotNull NexiaPlayer executor, @NotNull NexiaPlayer player) {

        DuelsPlayerData executorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (executor.equals(player)) {
            executor.sendMessage(Component.text("You cannot duel yourself!", ChatFormat.failColor));
            return;
        }

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.", ChatFormat.failColor));
            return;
        }

        if (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(executor)).gameMode != PlayerGameMode.LOBBY || ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!", ChatFormat.failColor));
            return;
        }

        DuelOptions.InviteOptions inviteOptions = playerData.inviteOptions;

        if (!inviteOptions.inviting || !inviteOptions.invitingPlayer.equals(executor)) {
            executor.sendMessage(Component.text("That player has not challenged you to a duel!", ChatFormat.failColor));
            return;
        }

        if(inviteOptions.customDuel) GamemodeHandler.joinCustomGamemode(player, executor, inviteOptions.inviteKit, inviteOptions.inviteMap, false);
        else GamemodeHandler.joinGamemode(player, executor, inviteOptions.inviteKit, inviteOptions.inviteMap, false);
    }

    public static void declineDuel(@NotNull NexiaPlayer executor, @NotNull NexiaPlayer player) {
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (executor.equals(player)) {
            executor.sendMessage(Component.text("You cannot duel yourself!", ChatFormat.failColor));
            return;
        }

        if (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(executor)).gameMode != PlayerGameMode.LOBBY || ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!", ChatFormat.failColor));
            return;
        }

        DuelOptions.InviteOptions inviteOptions = playerData.inviteOptions;

        if (!inviteOptions.inviting || !inviteOptions.invitingPlayer.equals(executor)) {
            executor.sendMessage(Component.text("That player has not challenged you to a duel!", ChatFormat.failColor));
            return;
        }

        inviteOptions.reset();


        player.sendNexiaMessage(Component.text(executor.getRawName() + " has declined your duel.", ChatFormat.normalColor));

        executor.sendNexiaMessage(
                Component.text("You have declined ", ChatFormat.normalColor)
                        .append(Component.text(player.getRawName(), ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                        .append(Component.text("'s duel.", ChatFormat.normalColor))
        );
    }

    public static void challengePlayer(NexiaPlayer executor, NexiaPlayer player, String stringGameMode, @Nullable DuelsMap selectedmap) {
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            executor.sendMessage(Component.text("Invalid gamemode!", ChatFormat.failColor));
            return;
        }
        if (executor.equals(player)) {
            executor.sendMessage(Component.text("You cannot duel yourself!", ChatFormat.failColor));
            return;
        }

        DuelsPlayerData executorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.", ChatFormat.failColor));
            return;
        }

        if (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!", ChatFormat.failColor));
            return;
        }

        if (executorData.duelOptions.duelsTeam != null && !executorData.duelOptions.duelsTeam.isLeader(executor)) {
            executor.sendMessage(Component.text("You are not the team leader!", ChatFormat.failColor));
            return;
        }

        DuelsMap map = selectedmap;
        if (map == null) {
            map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
            while(!map.isAdventureSupported && gameMode.gameMode.equals(Minecraft.GameMode.ADVENTURE)) {
                map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
            }
        } else {
            if (!DuelsMap.duelsMaps.contains(map)) {
                executor.sendMessage(Component.text("Invalid map!", ChatFormat.failColor));
                return;
            }
        }
        if(gameMode.gameMode == Minecraft.GameMode.ADVENTURE && !map.isAdventureSupported) {
            executor.sendMessage(Component.text("This map is not supported for this gamemode!", ChatFormat.failColor));
            return;
        }

        DuelOptions.InviteOptions inviteOptions = executorData.inviteOptions;

        inviteOptions.inviteMap = map;
        inviteOptions.inviteKit = stringGameMode.toUpperCase();
        inviteOptions.inviting = true;
        inviteOptions.invitingPlayer = player;
        inviteOptions.customDuel = false;

        // } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(executor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){

        Component message = Component.text(executor.getRawName(), ChatFormat.brandColor1)
                .append(Component.text(" has challenged you to a duel!", ChatFormat.normalColor)
                );

        if (executorData.duelOptions.duelsTeam == null) {
            executor.sendNexiaMessage(
                    Component.text("Sending a duel request to ", ChatFormat.normalColor)
                            .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase(), ChatFormat.brandColor2))
                            .append(Component.text(" with kit ", ChatFormat.normalColor))
                            .append(Component.text(stringGameMode, ChatFormat.brandColor2))
                            .append(Component.text(".", ChatFormat.normalColor))
            );
        } else {
            executor.sendNexiaMessage(
                    Component.text("Sending a ", ChatFormat.normalColor)
                            .append(Component.text("team duel", ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                            .append(Component.text(" request to ", ChatFormat.normalColor))
                            .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase(), ChatFormat.brandColor2))
                            .append(Component.text(" with kit ", ChatFormat.normalColor))
                            .append(Component.text(stringGameMode, ChatFormat.brandColor2))
                            .append(Component.text(".", ChatFormat.normalColor))
            );
            
            message = Component.text(executor.getRawName(), ChatFormat.brandColor1)
                    .append(Component.text(" has challenged you to a ", ChatFormat.normalColor))
                    .append(Component.text("team duel", ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                    .append(Component.text("!", ChatFormat.normalColor));
        }


        Component kit = Component.text("Kit: ", ChatFormat.brandColor1)
                .append(Component.text(stringGameMode.toUpperCase(), ChatFormat.normalColor)
                );

        Component mapName = Component.text("Map: ", ChatFormat.brandColor1)
                .append(Component.text(map.id.toUpperCase(), ChatFormat.normalColor)
                );

        Component yes = Component.text("[").color(ChatFormat.Minecraft.dark_gray)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/acceptduel " + executor.getRawName())))
                .append(Component.text("]  ", ChatFormat.Minecraft.dark_gray)
                );

        Component no = Component.text("[").color(ChatFormat.Minecraft.dark_gray)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/declineduel " + executor.getRawName())))
                .append(Component.text("]  ", ChatFormat.Minecraft.dark_gray)
                );


        player.sendMessage(message);
        player.sendMessage(kit);
        player.sendMessage(mapName);
        player.sendMessage(yes.append(no));
    }

    public static void customChallengePlayer(NexiaPlayer executor, NexiaPlayer player, String customKit, @Nullable DuelsMap selectedmap) {
        DuelsPlayerData executorData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor);
        DuelOptions.InviteOptions inviteOptions = executorData.inviteOptions;

        if (executor.equals(player)) {
            executor.sendMessage(Component.text("You cannot duel yourself!", ChatFormat.failColor));
            return;
        }

        if(customKit.equalsIgnoreCase("vanilla") || customKit.equalsIgnoreCase("smp")) ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(executor)).inviteOptions.inviteKit2 = customKit;

        if (!DuelGameHandler.validCustomKit(executor, customKit)) {
            executor.sendMessage(Component.text("Invalid kit!", ChatFormat.failColor));
            return;
        }

        if(inviteOptions.inviteKit2 != null && !DuelGameHandler.validCustomKit(player, inviteOptions.inviteKit2)) {
            executor.sendMessage(Component.text("The other player does not have a valid custom kit for " + customKit + ".", ChatFormat.failColor));
            inviteOptions.inviteKit2 = null;
            return;
        }
        
        DuelsPlayerData playerData = (DuelsPlayerData) PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player);

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.", ChatFormat.failColor));
            return;
        }

        if (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!", ChatFormat.failColor));
            return;
        }

        if (executorData.duelOptions.duelsTeam != null && !executorData.duelOptions.duelsTeam.isLeader(executor)) {
            executor.sendMessage(Component.text("You are not the team leader!", ChatFormat.failColor));
            return;
        }

        DuelsMap map = selectedmap;
        if (map == null) {
            map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
        } else {
            if (!DuelsMap.duelsMaps.contains(map)) {
                executor.sendMessage(Component.text("Invalid map!", ChatFormat.failColor));
                return;
            }
        }

        inviteOptions.inviteMap = map;
        inviteOptions.inviteKit = customKit.toLowerCase();
        inviteOptions.inviting = true;
        inviteOptions.invitingPlayer = player;
        inviteOptions.customDuel = true;

        inviteOptions.perCustomDuel = inviteOptions.inviteKit2 != null && !inviteOptions.inviteKit2.trim().isEmpty();

        // } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(executor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){

        Component message = Component.text(executor.getRawName(), ChatFormat.brandColor1)
                .append(Component.text(" has challenged you to a duel!", ChatFormat.normalColor)
                );

        if (executorData.duelOptions.duelsTeam == null) {
            executor.sendNexiaMessage(
                    Component.text("Sending a " + (inviteOptions.perCustomDuel ? "per-" : "") + "custom duel request to ", ChatFormat.normalColor)
                            .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase(), ChatFormat.brandColor2))
                            .append(Component.text(" with " + "custom kit ", ChatFormat.normalColor))
                            .append(Component.text(customKit, ChatFormat.brandColor2))
                            .append(Component.text(".", ChatFormat.normalColor))
            );
        } else {
            executor.sendNexiaMessage(
                    Component.text("Sending a ", ChatFormat.normalColor)
                            .append(Component.text((inviteOptions.perCustomDuel ? "per-" : "") + "custom team duel", ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                            .append(Component.text(" request to ", ChatFormat.normalColor))
                            .append(Component.text(player.getRawName(), ChatFormat.brandColor2))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase(), ChatFormat.brandColor2))
                            .append(Component.text(" with custom kit ", ChatFormat.normalColor))
                            .append(Component.text(customKit, ChatFormat.brandColor2))
                            .append(Component.text(".", ChatFormat.normalColor))
                    );

            message = Component.text(executor.getRawName(), ChatFormat.brandColor1)
                    .append(Component.text(" has challenged you to a ", ChatFormat.normalColor))
                    .append(Component.text((inviteOptions.perCustomDuel ? "per-" : "") + "custom team duel", ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                    .append(Component.text("!", ChatFormat.normalColor));
        }


        Component kit = Component.text("Custom Kit: ", ChatFormat.brandColor1)
                .append(Component.text(customKit.toLowerCase(), ChatFormat.normalColor)
                );

        Component mapName = Component.text("Map: ", ChatFormat.brandColor1)
                .append(Component.text(map.id.toUpperCase(), ChatFormat.normalColor)
                );

        Component yes = Component.text("[", ChatFormat.Minecraft.dark_gray)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/acceptduel " + executor.getRawName())))
                .append(Component.text("]  ", ChatFormat.Minecraft.dark_gray)
                );

        Component no = Component.text("[", ChatFormat.Minecraft.dark_gray)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me", ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/declineduel " + executor.getRawName())))
                .append(Component.text("]  ", ChatFormat.Minecraft.dark_gray)
                );


        player.sendMessage(message);
        player.sendMessage(kit);
        player.sendMessage(mapName);
        player.sendMessage(yes.append(no));
    }
}
