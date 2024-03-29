package com.nexia.minigames.games.duels.gamemodes;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.custom.CustomDuelsGame;
import com.nexia.minigames.games.duels.custom.team.CustomTeamDuelsGame;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.team.DuelsTeam;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.DuelOptions;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.notcoded.codelib.players.AccuratePlayer;
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

    public static boolean isInQueue(@NotNull ServerPlayer player, @NotNull DuelGameMode gameMode) {
        return gameMode.queue.contains(player);
    }

    public static void joinQueue(ServerPlayer minecraftPlayer, String stringGameMode, boolean silent) {
        if (stringGameMode.equalsIgnoreCase("lobby") || stringGameMode.equalsIgnoreCase("leave")) {
            LobbyUtil.leaveAllGames(minecraftPlayer, true);
            return;
        }

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);

        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        if (gameMode == null) {
            if (!silent) player.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            return;
        }

        PlayerData data = PlayerDataManager.get(minecraftPlayer);

        if (data.duelOptions.duelsTeam != null) {
            if (!silent) player.sendMessage(Component.text("You are in a team!").color(ChatFormat.failColor));
            return;
        }


        if (!silent) {
            player.sendMessage(
                    Component.text("You have queued up for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".").decoration(ChatFormat.bold, false))
            );

        }

        removeQueue(minecraftPlayer, stringGameMode, true);

        gameMode.queue.add(minecraftPlayer);
        if (gameMode.queue.size() >= 2) {
            GamemodeHandler.joinGamemode(minecraftPlayer, gameMode.queue.get(0), stringGameMode, null, false);
        }
    }

    public static void removeQueue(ServerPlayer minecraftPlayer, @Nullable String stringGameMode, boolean silent) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        if (stringGameMode != null) {
            DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
            if (gameMode == null) {
                if (!silent) player.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
                return;
            }

            gameMode.queue.remove(minecraftPlayer);

            if (!silent) {
                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("You have left the queue for ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                                        .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                ));
            }
        } else {
            for(DuelGameMode gameMode : DuelGameMode.duelGameModes) {
                gameMode.queue.remove(minecraftPlayer);
            }
        }
    }


    public static void spectatePlayer(@NotNull AccuratePlayer executor, @NotNull AccuratePlayer player) {
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor.get());
        if (executor == player) {
            factoryExecutor.sendMessage(Component.text("You may not spectate yourself!").color(ChatFormat.failColor));
            return;
        }

        PlayerData playerData = PlayerDataManager.get(player.get());

        if ((!playerData.inDuel && playerData.gameOptions.teamDuelsGame == null && playerData.gameOptions.duelsGame == null) || playerData.gameOptions == null) {
            factoryExecutor.sendMessage(Component.text("That player is not in a duel!").color(ChatFormat.failColor));
            return;
        }


        PlayerData executorData = PlayerDataManager.get(executor.get());

        if (executorData.gameMode == DuelGameMode.SPECTATING) {
            unspectatePlayer(executor, player, false);
        }

        /*
        if(playerData.teamDuelsGame != null) {
            factoryExecutor.sendMessage(Component.text("Spectating Team Duels is currently not available. We are sorry for the inconvenience.").color(ChatFormat.failColor));
            return;
        }
         */
        // what could go wrong?

        factoryExecutor.setGameMode(Minecraft.GameMode.SPECTATOR);
        executor.get().teleportTo(player.get().getLevel(), player.get().getX(), player.get().getY(), player.get().getZ(), 0, 0);

        DuelsGame duelsGame = playerData.gameOptions.duelsGame;
        CustomDuelsGame customDuelsGame = playerData.gameOptions.customDuelsGame;
        TeamDuelsGame teamDuelsGame = playerData.gameOptions.teamDuelsGame;
        CustomTeamDuelsGame customTeamDuelsGame = playerData.gameOptions.customTeamDuelsGame;

        TextComponent spectateMSG = new TextComponent("§7§o(" + factoryExecutor.getRawName() + " started spectating)");

        if (teamDuelsGame != null) {
            teamDuelsGame.spectators.add(executor);
            List<AccuratePlayer> everyTeamMember = teamDuelsGame.team1.all;
            everyTeamMember.addAll(teamDuelsGame.team2.all);
            for (AccuratePlayer players : everyTeamMember) {
                players.get().sendMessage(spectateMSG, Util.NIL_UUID);
            }
        } else if (duelsGame != null) {
            duelsGame.spectators.add(executor);
            duelsGame.p1.get().sendMessage(spectateMSG, Util.NIL_UUID);
            duelsGame.p2.get().sendMessage(spectateMSG, Util.NIL_UUID);
        } else if (customTeamDuelsGame != null) {
            customTeamDuelsGame.spectators.add(executor);
            List<AccuratePlayer> everyTeamMember = customTeamDuelsGame.team1.all;
            everyTeamMember.addAll(customTeamDuelsGame.team2.all);
            for (AccuratePlayer players : everyTeamMember) {
                players.get().sendMessage(spectateMSG, Util.NIL_UUID);
            }
        } else if (customDuelsGame != null) {
            customDuelsGame.spectators.add(executor);
            customDuelsGame.p1.get().sendMessage(spectateMSG, Util.NIL_UUID);
            customDuelsGame.p2.get().sendMessage(spectateMSG, Util.NIL_UUID);
        }


        factoryExecutor.sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You are now spectating ")
                                .color(ChatFormat.normalColor)
                                .decoration(ChatFormat.bold, false)
                                .append(Component.text(player.get().getScoreboardName())
                                        .color(ChatFormat.brandColor1)
                                        .decoration(ChatFormat.bold, true)
                                )
                        )
        );

        executorData.duelOptions.spectatingPlayer = player;
        executorData.gameMode = DuelGameMode.SPECTATING;
    }

    public static void unspectatePlayer(@NotNull AccuratePlayer executor, @Nullable AccuratePlayer player, boolean teleport) {
        PlayerData playerData = null;

        if (player != null) {
            playerData = PlayerDataManager.get(player.get());
        }

        DuelsGame duelsGame = null;
        CustomDuelsGame customDuelsGame = null;
        TeamDuelsGame teamDuelsGame = null;
        CustomTeamDuelsGame customTeamDuelsGame = null;

        if (player != null && playerData.inDuel && (playerData.gameOptions != null && playerData.gameOptions.duelsGame != null)) {
            duelsGame = playerData.gameOptions.duelsGame;
        } else if (player != null && playerData.inDuel && (playerData.gameOptions != null && playerData.gameOptions.teamDuelsGame != null)) {
            teamDuelsGame = playerData.gameOptions.teamDuelsGame;
        } else if (player != null && playerData.inDuel && (playerData.gameOptions != null && playerData.gameOptions.customDuelsGame != null)) {
            customDuelsGame = playerData.gameOptions.customDuelsGame;
        } else if (player != null && playerData.inDuel && (playerData.gameOptions != null && playerData.gameOptions.customTeamDuelsGame != null)) {
            customTeamDuelsGame = playerData.gameOptions.customTeamDuelsGame;
        }

        PlayerData executorData = PlayerDataManager.get(executor.get());
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor.get());

        TextComponent spectateMSG = new TextComponent("§7§o(" + factoryExecutor.getRawName() + " has stopped spectating)");
        if (duelsGame != null || teamDuelsGame != null || customDuelsGame != null || customTeamDuelsGame != null) {
            factoryExecutor.sendMessage(
                    ChatFormat.nexiaMessage
                            .append(Component.text("You have stopped spectating ")
                                    .color(ChatFormat.normalColor)
                                    .decoration(ChatFormat.bold, false)
                                    .append(Component.text(player.get().getScoreboardName())
                                            .color(ChatFormat.brandColor1)
                                            .decoration(ChatFormat.bold, true)
                                    )
                            )
            );
        }

        if (duelsGame != null) {
            duelsGame.spectators.remove(executor);

            duelsGame.p1.get().sendMessage(spectateMSG, Util.NIL_UUID);
            duelsGame.p2.get().sendMessage(spectateMSG, Util.NIL_UUID);
        } else if (teamDuelsGame != null) {
            teamDuelsGame.spectators.remove(executor);

            List<AccuratePlayer> everyTeamPlayer = teamDuelsGame.team1.all;
            everyTeamPlayer.addAll(teamDuelsGame.team2.all);

            for (AccuratePlayer players : everyTeamPlayer) {
                players.get().sendMessage(spectateMSG, Util.NIL_UUID);
            }
        } else if (customDuelsGame != null) {
            customDuelsGame.spectators.remove(executor);

            customDuelsGame.p1.get().sendMessage(spectateMSG, Util.NIL_UUID);
            customDuelsGame.p2.get().sendMessage(spectateMSG, Util.NIL_UUID);
        } else if (customTeamDuelsGame != null) {
            customTeamDuelsGame.spectators.remove(executor);

            List<AccuratePlayer> everyTeamPlayer = customTeamDuelsGame.team1.all;
            everyTeamPlayer.addAll(customTeamDuelsGame.team2.all);

            for (AccuratePlayer players : everyTeamPlayer) {
                players.get().sendMessage(spectateMSG, Util.NIL_UUID);
            }
        }


        executorData.gameMode = DuelGameMode.LOBBY;
        executorData.duelOptions.spectatingPlayer = null;
        LobbyUtil.leaveAllGames(executor.get(), teleport);
    }
    public static void joinGamemode(ServerPlayer invitor, ServerPlayer player, String stringGameMode, @Nullable DuelsMap selectedmap, boolean silent) {
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            }
            return;
        }
        PlayerData data = PlayerDataManager.get(invitor);
        PlayerData playerData = PlayerDataManager.get(player);

        if (data.duelOptions.duelsTeam != null && data.duelOptions.duelsTeam.getPeople().contains(AccuratePlayer.create(player))) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("You cannot duel people on your team!").color(ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam != null && !data.duelOptions.duelsTeam.isLeader(AccuratePlayer.create(invitor))) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam == null && playerData.duelOptions.duelsTeam != null) {
            DuelsTeam.createTeam(invitor, false);
        }

        if (data.duelOptions.duelsTeam != null && playerData.duelOptions.duelsTeam != null) {
            TeamDuelsGame.startGame(data.duelOptions.duelsTeam, playerData.duelOptions.duelsTeam, stringGameMode, selectedmap);
            return;
        }

        DuelsGame.startGame(invitor, player, stringGameMode, selectedmap);

    }

    public static void joinCustomGamemode(ServerPlayer invitor, ServerPlayer player, String kitID, @Nullable DuelsMap selectedmap, boolean silent) {
        if (!DuelGameHandler.validCustomKit(invitor, kitID)) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("Invalid custom kit!").color(ChatFormat.failColor));
            }
            return;
        }

        PlayerData data = PlayerDataManager.get(invitor);
        PlayerData playerData = PlayerDataManager.get(player);

        if(data.inviteOptions.inviteKit2 != null && !DuelGameHandler.validCustomKit(player, data.inviteOptions.inviteKit2)) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("Invalid per-custom kit (2)!").color(ChatFormat.failColor));
            }
            return;
        } else if(data.inviteOptions.inviteKit2 != null && DuelGameHandler.validCustomKit(player, data.inviteOptions.inviteKit2)){
            data.inviteOptions.perCustomDuel = true;
        }
        
        if (data.duelOptions.duelsTeam != null && data.duelOptions.duelsTeam.getPeople().contains(AccuratePlayer.create(player))) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("You cannot duel people on your team!").color(ChatFormat.failColor));
            }
            return;
        }

        if (data.duelOptions.duelsTeam != null && !data.duelOptions.duelsTeam.isLeader(AccuratePlayer.create(invitor))) {
            if (!silent) {
                PlayerUtil.getFactoryPlayer(invitor).sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
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

    public static void acceptDuel(@NotNull ServerPlayer minecraftExecutor, @NotNull ServerPlayer minecraftPlayer) {
        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);
        //Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftExecutor).gameMode != PlayerGameMode.LOBBY || com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        DuelOptions.InviteOptions inviteOptions = playerData.inviteOptions;

        if (!inviteOptions.inviting || !inviteOptions.invitingPlayer.equals(AccuratePlayer.create(minecraftExecutor))) {
            executor.sendMessage(Component.text("That player has not challenged you to a duel!").color(ChatFormat.failColor));
            return;
        }

        if(inviteOptions.customDuel) GamemodeHandler.joinCustomGamemode(minecraftPlayer, minecraftExecutor, inviteOptions.inviteKit, inviteOptions.inviteMap, false);
        else GamemodeHandler.joinGamemode(minecraftPlayer, minecraftExecutor, inviteOptions.inviteKit, inviteOptions.inviteMap, false);
    }

    public static void declineDuel(@NotNull ServerPlayer minecraftExecutor, @NotNull ServerPlayer minecraftPlayer) {
        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        //PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftExecutor).gameMode != PlayerGameMode.LOBBY || com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        DuelOptions.InviteOptions inviteOptions = playerData.inviteOptions;

        if (!inviteOptions.inviting || !inviteOptions.invitingPlayer.equals(AccuratePlayer.create(minecraftExecutor))) {
            executor.sendMessage(Component.text("That player has not challenged you to a duel!").color(ChatFormat.failColor));
            return;
        }

        inviteOptions.reset();


        player.sendMessage(ChatFormat.nexiaMessage.append(Component.text(executor.getRawName() + " has declined your duel.").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));

        executor.sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("You have declined ")
                        .color(ChatFormat.normalColor)
                        .decoration(ChatFormat.bold, false))
                .append(Component.text(player.getRawName())
                        .color(ChatFormat.brandColor1)
                        .decoration(ChatFormat.bold, true))
                .append(Component.text("'s duel.")
                        .color(ChatFormat.normalColor)
                        .decoration(ChatFormat.bold, false)
                )
        );
    }

    public static void challengePlayer(ServerPlayer minecraftExecutor, ServerPlayer minecraftPlayer, String stringGameMode, @Nullable DuelsMap selectedmap) {

        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);

        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if (gameMode == null) {
            executor.sendMessage(Component.text("Invalid gamemode!").color(ChatFormat.failColor));
            return;
        }
        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if (executorData.duelOptions.duelsTeam != null && !executorData.duelOptions.duelsTeam.isLeader(AccuratePlayer.create(minecraftExecutor))) {
            executor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        DuelsMap map = selectedmap;
        if (map == null) {
            map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
            while(!map.isAdventureSupported && gameMode.gameMode.equals(GameType.ADVENTURE)) {
                map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
            }
        } else {
            if (!DuelsMap.duelsMaps.contains(map)) {
                executor.sendMessage(Component.text("Invalid map!").color(ChatFormat.failColor));
                return;
            }
        }
        if(gameMode.gameMode == GameType.ADVENTURE && !map.isAdventureSupported) {
            executor.sendMessage(Component.text("This map is not supported for this gamemode!").color(ChatFormat.failColor));
            return;
        }

        DuelOptions.InviteOptions inviteOptions = executorData.inviteOptions;

        inviteOptions.inviteMap = map;
        inviteOptions.inviteKit = stringGameMode.toUpperCase();
        inviteOptions.inviting = true;
        inviteOptions.invitingPlayer = AccuratePlayer.create(minecraftPlayer);
        inviteOptions.customDuel = false;

        // } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(minecraftExecutor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){

        Component message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                .append(Component.text(" has challenged you to a duel!").color(ChatFormat.normalColor)
                );

        if (executorData.duelOptions.duelsTeam == null) {
            executor.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Sending a duel request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" with kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(stringGameMode).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        } else {
            executor.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Sending a ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text("team duel").color(ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                            .append(Component.text(" request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" with kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(stringGameMode).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                    .append(Component.text(" has challenged you to a ").color(ChatFormat.normalColor))
                    .append(Component.text("team duel").color(ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                    .append(Component.text("!").color(ChatFormat.normalColor));
        }


        Component kit = Component.text("Kit: ").color(ChatFormat.brandColor1)
                .append(Component.text(stringGameMode.toUpperCase()).color(ChatFormat.normalColor)
                );

        Component mapName = Component.text("Map: ").color(ChatFormat.brandColor1)
                .append(Component.text(map.id.toUpperCase()).color(ChatFormat.normalColor)
                );

        Component yes = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/acceptduel " + executor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );

        Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/declineduel " + executor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );


        player.sendMessage(message);
        player.sendMessage(kit);
        player.sendMessage(mapName);
        player.sendMessage(yes.append(no));
    }

    public static void customChallengePlayer(ServerPlayer minecraftExecutor, ServerPlayer minecraftPlayer, String customKit, @Nullable DuelsMap selectedmap) {

        Player executor = PlayerUtil.getFactoryPlayer(minecraftExecutor);

        if (!DuelGameHandler.validCustomKit(minecraftExecutor, customKit)) {
            executor.sendMessage(Component.text("Invalid kit!").color(ChatFormat.failColor));
            return;
        }

        if (minecraftExecutor == minecraftPlayer) {
            executor.sendMessage(Component.text("You cannot duel yourself!").color(ChatFormat.failColor));
            return;
        }

        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData executorData = PlayerDataManager.get(minecraftExecutor);
        PlayerData playerData = PlayerDataManager.get(minecraftPlayer);

        if (executorData.inDuel || playerData.inDuel) {
            executor.sendMessage(Component.text("That player is currently dueling someone.").color(ChatFormat.failColor));
            return;
        }

        if (com.nexia.core.utilities.player.PlayerDataManager.get(minecraftPlayer).gameMode != PlayerGameMode.LOBBY) {
            executor.sendMessage(Component.text("That player is not in duels!").color(ChatFormat.failColor));
            return;
        }

        if (executorData.duelOptions.duelsTeam != null && !executorData.duelOptions.duelsTeam.isLeader(AccuratePlayer.create(minecraftExecutor))) {
            executor.sendMessage(Component.text("You are not the team leader!").color(ChatFormat.failColor));
            return;
        }

        DuelsMap map = selectedmap;
        if (map == null) {
            map = DuelsMap.duelsMaps.get(RandomUtil.randomInt(DuelsMap.duelsMaps.size()));
        } else {
            if (!DuelsMap.duelsMaps.contains(map)) {
                executor.sendMessage(Component.text("Invalid map!").color(ChatFormat.failColor));
                return;
            }
        }

        DuelOptions.InviteOptions inviteOptions = executorData.inviteOptions;

        inviteOptions.inviteMap = map;
        inviteOptions.inviteKit = customKit.toLowerCase();
        inviteOptions.inviting = true;
        inviteOptions.invitingPlayer = AccuratePlayer.create(minecraftPlayer);
        inviteOptions.customDuel = true;


        // } else if((!executorData.inviteMap.equalsIgnoreCase(playerData.inviteMap) || !executorData.inviteKit.equalsIgnoreCase(playerData.inviteKit)) && (playerData.invitingPlayer == null || !playerData.invitingPlayer.getStringUUID().equalsIgnoreCase(minecraftExecutor.getStringUUID())) && playerData.gameMode == DuelGameMode.LOBBY){

        Component message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                .append(Component.text(" has challenged you to a duel!").color(ChatFormat.normalColor)
                );

        if (executorData.duelOptions.duelsTeam == null) {
            executor.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Sending a custom duel request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" with custom kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(customKit).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
        } else {
            executor.sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Sending a ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text("custom team duel").color(ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                            .append(Component.text(" request to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(player.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" on map ")).append(Component.text(map.id.toUpperCase()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(" with custom kit ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                            .append(Component.text(customKit).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                            .append(Component.text(".")).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)));
            message = Component.text(executor.getRawName()).color(ChatFormat.brandColor1)
                    .append(Component.text(" has challenged you to a ").color(ChatFormat.normalColor))
                    .append(Component.text("custom team duel").color(ChatFormat.normalColor).decoration(ChatFormat.bold, true))
                    .append(Component.text("!").color(ChatFormat.normalColor));
        }


        Component kit = Component.text("Custom Kit: ").color(ChatFormat.brandColor1)
                .append(Component.text(customKit.toLowerCase()).color(ChatFormat.normalColor)
                );

        Component mapName = Component.text("Map: ").color(ChatFormat.brandColor1)
                .append(Component.text(map.id.toUpperCase()).color(ChatFormat.normalColor)
                );

        Component yes = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("ACCEPT")
                        .color(ChatFormat.greenColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/acceptduel " + executor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );

        Component no = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("DECLINE")
                        .color(ChatFormat.failColor)
                        .decorate(ChatFormat.bold)
                        .hoverEvent(HoverEvent.showText(Component.text("Click me").color(ChatFormat.brandColor2)))
                        .clickEvent(ClickEvent.runCommand("/declineduel " + executor.getRawName())))
                .append(Component.text("]  ").color(NamedTextColor.DARK_GRAY)
                );


        player.sendMessage(message);
        player.sendMessage(kit);
        player.sendMessage(mapName);
        player.sendMessage(yes.append(no));
    }
}