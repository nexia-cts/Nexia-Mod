package com.nexia.minigames.games.duels;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.util.SavedDuelsData;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

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

    public static DuelsGame startGame(ServerPlayer mcP1, ServerPlayer mcP2, String stringGameMode, @Nullable String selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.FFA;
            System.out.printf("[ERROR] Nexia: Invalid duel gamemode ({0}) selected! Using fallback one.%n", stringGameMode);
        }

        Player p1 = PlayerUtil.getFactoryPlayer(mcP1);
        Player p2 = PlayerUtil.getFactoryPlayer(mcP2);

        ServerLevel duelLevel = DuelGameHandler.createWorld();
        if(selectedMap == null){
            selectedMap = com.nexia.minigames.Main.config.duelsMaps.get(RandomUtil.randomInt(0, com.nexia.minigames.Main.config.duelsMaps.size()-1));
        }
        String name = duelLevel.dimension().toString().replaceAll("]", "").split(":")[2];

        String mapid = "duels";

        String start = "/execute in " + mapid + ":" + name;

        ServerTime.factoryServer.runCommand(start + " run forceload add 0 0");
        ServerTime.factoryServer.runCommand(start + " run " + DuelGameHandler.returnCommandMap(selectedMap));
        ServerTime.factoryServer.runCommand(start + " run setblock 1 80 0 minecraft:redstone_block");

        ServerTime.factoryServer.runCommand(start + " if block 0 80 0 minecraft:structure_block run setblock 0 80 0 air");
        ServerTime.factoryServer.runCommand(start + " if block 1 80 0 minecraft:redstone_block run setblock 1 80 0 air");

        PlayerData invitorData = PlayerDataManager.get(mcP1);
        PlayerData playerData = PlayerDataManager.get(mcP2);

        p1.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        p1.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

        p2.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        p2.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);

        PlayerUtil.resetHealthStatus(p1);
        PlayerUtil.resetHealthStatus(p2);

        float[] invitorpos = DuelGameHandler.returnPosMap(selectedMap, true);
        float[] playerpos = DuelGameHandler.returnPosMap(selectedMap, false);

        mcP2.teleportTo(duelLevel, playerpos[0], playerpos[1], playerpos[2], playerpos[3], playerpos[4]);
        //EntityPos playerPos = new EntityPos(0, 85, 0, 0, 0);
        //mcP2.setRespawnPosition(duelLevel.dimension(), playerPos.toBlockPos(), playerPos.yaw, true, false);
        //mcP2.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);
        playerData.inviting = false;
        playerData.invitingPlayer = null;
        playerData.inDuel = true;
        playerData.duelPlayer = mcP1;

        mcP1.teleportTo(duelLevel, invitorpos[0], invitorpos[1], invitorpos[2], invitorpos[3], invitorpos[4]);
        //EntityPos invitorPos = new EntityPos(0, 85, 0, 0, 0);
        //mcP1.setRespawnPosition(duelLevel.dimension(), invitorPos.toBlockPos(), invitorPos.yaw, true, false);
        //mcP1.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);
        invitorData.inviting = false;
        invitorData.invitingPlayer = null;
        invitorData.inDuel = true;
        invitorData.duelPlayer = mcP2;

        p1.setGameMode(Minecraft.GameMode.SURVIVAL);
        p2.setGameMode(Minecraft.GameMode.SURVIVAL);

        removeQueue(mcP1, null, true);
        removeQueue(mcP2, null, true);

        /*
        InventoryUtil.setInventory(player, stringGameMode.toLowerCase(), "/duels", true);
        InventoryUtil.setInventory(invitor, stringGameMode.toLowerCase(), "/duels", true);
         */


        p2.sendMessage(ChatFormat.nexiaMessage()
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p1.getRawName()).color(ChatFormat.brandColor2))));

        p1.sendMessage(ChatFormat.nexiaMessage()
                .append(Component.text("Your opponent: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                .append(Component.text(p2.getRawName()).color(ChatFormat.brandColor2))));

        p1.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);
        p2.runCommand("/loadinventory " + stringGameMode.toLowerCase(), 4, false);

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;

        DuelsGame game = new DuelsGame(mcP1, mcP2, gameMode, selectedMap, duelLevel);
        invitorData.duelsGame = game;
        playerData.duelsGame = game;

        DuelGameHandler.duelsGames.add(game);

        return game;
    }

    public static void endGame(@NotNull ServerPlayer minecraftVictim, @Nullable ServerPlayer minecraftAttacker, boolean wait) {
        PlayerData victimData = PlayerDataManager.get(minecraftVictim);

        boolean attackerNull = minecraftAttacker == null;

        PlayerData attackerData = null;

        if (!attackerNull) {
            attackerData = PlayerDataManager.get(minecraftAttacker);
        }

        ServerLevel duelLevel;

        if (!attackerNull) {
            duelLevel = minecraftAttacker.getLevel();
        } else {
            duelLevel = minecraftVictim.getLevel();
        }

        Player victim = PlayerUtil.getFactoryPlayer(minecraftVictim);
        Player attacker = null;
        if (!attackerNull) {
            attacker = PlayerUtil.getFactoryPlayer(minecraftAttacker);
        }


        victimData.inviting = false;
        victimData.inDuel = false;
        victimData.inviteMap = "";
        victimData.inviteKit = "";
        removeQueue(minecraftVictim, victimData.gameMode.id, true);
        victimData.gameMode = DuelGameMode.LOBBY;

        if (!attackerNull) {
            attackerData.inviting = false;
            attackerData.inDuel = false;
            attackerData.inviteKit = "";
            attackerData.inviteMap = "";
            attackerData.gameMode = DuelGameMode.LOBBY;
            attackerData.duelsGame = null;

            attackerData.savedData.wins++;
            victimData.savedData.loss++;
        }


        //minecraftVictim.setGameMode(GameType.SPECTATOR);
        //victim.teleport(attacker.getLocation());

        Component win = Component.text("The game was a ")
                .color(ChatFormat.normalColor)
                .append(Component.text("draw").color(ChatFormat.brandColor2))
                .append(Component.text("!").color(ChatFormat.normalColor)
                );


        if (!attackerNull) {
            win = Component.text(attacker.getRawName()).color(ChatFormat.brandColor2)
                    .append(Component.text(" has won the duel!").color(ChatFormat.normalColor)
                    );
        }


        if (!attackerNull) {
            attacker.sendMessage(win);
            minecraftAttacker.die(DamageSource.GENERIC);
            PlayerUtil.resetHealthStatus(attacker);

            //LobbyUtil.sendGame(minecraftAttacker, "duels", false, false);

            attacker.getInventory().clear();
            minecraftAttacker.setGameMode(GameType.ADVENTURE);
        }

        victim.sendMessage(win);
        PlayerUtil.resetHealthStatus(victim);
        victim.getInventory().clear();

        SavedDuelsData data =
                new SavedDuelsData(
                        attackerData.duelPlayer, victimData.duelPlayer
                );
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ServerPlayer player = data.getLoser();
                ServerLevel level = player.getLevel();
                level.getServer().getCommands().performCommand(player.createCommandSourceStack(), "/hub");

                player = data.getWinner();
                level = player.getLevel();
                level.getServer().getCommands().performCommand(player.createCommandSourceStack(), "/hub");
            }
        }, 100L);

        minecraftVictim.setGameMode(GameType.ADVENTURE);
        DuelGameHandler.duelsGames.remove(victimData.duelsGame);
        victimData.duelsGame = null;

        DuelGameHandler.deleteWorld(duelLevel.dimension().toString().replaceAll("]", "").split(":")[2]);
    }

    public static void death(@NotNull ServerPlayer victim, @Nullable DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(source != null && source.getEntity() instanceof ServerPlayer attacker){
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if((victimData.inDuel && attackerData.inDuel) && victimData.duelsGame == attackerData.duelsGame){
                endGame(victim, attacker, true);
            }
            return;
        }
        if((source == null || !(source.getEntity() instanceof ServerPlayer)) && PlayerDataManager.get(victim).duelPlayer != null) {
            ServerPlayer attacker = victimData.duelPlayer;
            PlayerData attackerData = PlayerDataManager.get(attacker);

            if ((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                endGame(victim, attacker, true);
            }
            return;
        }
        if(victimData.inDuel) {
            endGame(victim, null, false);
        }

    }
}