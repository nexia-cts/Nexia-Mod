package com.nexia.minigames.games.duels;

import com.combatreforged.factory.api.event.player.PlayerDeathEvent;
import com.combatreforged.factory.api.world.entity.Entity;
import com.combatreforged.factory.api.world.entity.player.Player;
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
import net.kyori.adventure.text.Component;
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

    public static DuelsGame startGame(ServerPlayer mcP1, ServerPlayer mcP2, String stringGameMode, @Nullable String selectedMap){
        DuelGameMode gameMode = GamemodeHandler.identifyGamemode(stringGameMode);
        if(gameMode == null){
            gameMode = DuelGameMode.FFA; // fallback gamemode incase somehow
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

        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " run forceload add 0 0");
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " run " + DuelGameHandler.returnCommandMap(selectedMap));
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " run setblock 1 80 0 minecraft:redstone_block");

        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " if block 0 80 0 minecraft:structure_block run setblock 0 80 0 air");
        Main.server.getCommands().performCommand(Main.server.createCommandSourceStack(), "/execute in " + mapid + ":" + name + " if block 1 80 0 minecraft:redstone_block run setblock 1 80 0 air");


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
        EntityPos playerPos = new EntityPos(0, 85, 0, 0, 0);
        //mcP2.setRespawnPosition(duelLevel.dimension(), playerPos.toBlockPos(), playerPos.yaw, true, false);
        mcP2.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);
        playerData.inviting = false;
        playerData.invitingPlayer = null;
        playerData.inDuel = true;
        playerData.duelPlayer = mcP1;

        mcP1.teleportTo(duelLevel, invitorpos[0], invitorpos[1], invitorpos[2], invitorpos[3], invitorpos[4]);
        EntityPos invitorPos = new EntityPos(0, 85, 0, 0, 0);
        //mcP1.setRespawnPosition(duelLevel.dimension(), invitorPos.toBlockPos(), invitorPos.yaw, true, false);
        mcP1.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);
        invitorData.inviting = false;
        invitorData.invitingPlayer = null;
        invitorData.inDuel = true;
        invitorData.duelPlayer = mcP2;

        mcP1.setGameMode(GameType.SURVIVAL);
        mcP2.setGameMode(GameType.SURVIVAL);

        removeQueue(mcP1, null, true);
        removeQueue(mcP2, null, true);

        /*
        InventoryUtil.setInventory(player, stringGameMode.toLowerCase(), "/duels", true);
        InventoryUtil.setInventory(invitor, stringGameMode.toLowerCase(), "/duels", true);
         */


        p2.sendMessage(ChatFormat.nexiaMessage().append(Component.text("Your opponent: ").color(ChatFormat.normalColor).append(Component.text(p1.getRawName()).color(ChatFormat.brandColor2))));
        p1.sendMessage(ChatFormat.nexiaMessage().append(Component.text("Your opponent: ").color(ChatFormat.normalColor).append(Component.text(p2.getRawName()).color(ChatFormat.brandColor2))));

        ServerTime.minecraftServer.getCommands().performCommand(ServerTime.minecraftServer.createCommandSourceStack(), "/execute as " + p1.getRawName() + " run loadinventory " + stringGameMode.toLowerCase() + " " + p1.getRawName());
        ServerTime.minecraftServer.getCommands().performCommand(ServerTime.minecraftServer.createCommandSourceStack(), "/execute as " + p2.getRawName() + " run loadinventory " + stringGameMode.toLowerCase() + " " + p2.getRawName());

        playerData.gameMode = gameMode;
        invitorData.gameMode = gameMode;

        DuelsGame game = new DuelsGame(mcP1, mcP2, gameMode, selectedMap, duelLevel);

        invitorData.duelsGame = game;
        playerData.duelsGame = game;

        return game;
    }


    public static void endGame(@NotNull ServerPlayer minecraftVictim, @NotNull ServerPlayer minecraftAttacker, boolean wait) {
        PlayerData victimData = PlayerDataManager.get(minecraftVictim);
        PlayerData attackerData = PlayerDataManager.get(minecraftAttacker);

        ServerLevel duelLevel = minecraftAttacker.getLevel();

        Player victim = PlayerUtil.getFactoryPlayer(minecraftVictim);
        Player attacker = PlayerUtil.getFactoryPlayer(minecraftAttacker);

        victimData.inviting = false;
        victimData.inDuel = false;
        victimData.duelPlayer = null;
        victimData.inviteMap = "";
        victimData.inviteKit = "";
        removeQueue(minecraftVictim, victimData.gameMode.id, true);
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

        minecraftVictim.setGameMode(GameType.SPECTATOR);
        victim.teleport(attacker.getLocation());

        Component win = ChatFormat.returnAppendedComponent(
                Component.text(attacker.getRawName()).color(ChatFormat.brandColor2),
                Component.text(" has won the duel!")
        );

        attacker.sendMessage(win);
        victim.sendMessage(win);

        //minecraftAttacker.teleportTo(DuelsSpawn.duelWorld, DuelsSpawn.spawn.x, DuelsSpawn.spawn.y, DuelsSpawn.spawn.z, DuelsSpawn.spawn.yaw, DuelsSpawn.spawn.pitch)
        //minecraftAttacker.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);

        attacker.kill();

        //minecraftVictim.teleportTo(DuelsSpawn.duelWorld, DuelsSpawn.spawn.x, DuelsSpawn.spawn.y, DuelsSpawn.spawn.z, DuelsSpawn.spawn.yaw, DuelsSpawn.spawn.pitch);
        //minecraftVictim.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);

        PlayerUtil.resetHealthStatus(attacker);
        PlayerUtil.resetHealthStatus(victim);

        // Fix command bug (/duel & /queue being red indicating you can't use them, but you actually still can)
        LobbyUtil.sendGame(minecraftVictim, "duels", false, false);
        LobbyUtil.sendGame(minecraftAttacker, "duels", false, false);

        attacker.getInventory().clear();
        victim.getInventory().clear();

        minecraftAttacker.setGameMode(GameType.ADVENTURE);
        minecraftVictim.setGameMode(GameType.ADVENTURE);

        DuelGameHandler.deleteWorld(duelLevel.dimension().toString().replaceAll("]", "").split(":")[2]);
    }

    public static void death(@NotNull ServerPlayer victim, @Nullable PlayerDeathEvent event, @Nullable DamageSource legacy){
        if(event != null){

            Entity entityAttacker = event.getCause().getDamagingEntity();

            if(event.getCause().getDamagingEntity() instanceof Player factoryAttacker){
                ServerPlayer mcAttacker = PlayerUtil.getMinecraftPlayer(factoryAttacker);
                PlayerData victimData = PlayerDataManager.get(victim);
                PlayerData attackerData = PlayerDataManager.get(mcAttacker);

                if((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(mcAttacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                    DuelsGame.endGame(victim, mcAttacker, true);
                }
            } else if(!(entityAttacker instanceof Player) && PlayerDataManager.get(victim).duelPlayer != null){
                PlayerData victimData = PlayerDataManager.get(victim);
                ServerPlayer attacker = victimData.duelPlayer;
                PlayerData attackerData = PlayerDataManager.get(attacker);

                if ((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                    DuelsGame.endGame(victim, attacker, true);
                }
            }

        } else if(legacy != null){
            if(legacy.getEntity() instanceof ServerPlayer attacker){
                PlayerData victimData = PlayerDataManager.get(victim);
                PlayerData attackerData = PlayerDataManager.get(attacker);

                if((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                    DuelsGame.endGame(victim, attacker, true);
                }
            }
            if(!(legacy.getEntity() instanceof ServerPlayer) && PlayerDataManager.get(victim).duelPlayer != null) {
                PlayerData victimData = PlayerDataManager.get(victim);
                ServerPlayer attacker = victimData.duelPlayer;
                PlayerData attackerData = PlayerDataManager.get(attacker);

                if ((victimData.inDuel && attackerData.inDuel) && (victimData.duelPlayer.getStringUUID().equalsIgnoreCase(attacker.getStringUUID())) && attackerData.duelPlayer.getStringUUID().equalsIgnoreCase(victim.getStringUUID())) {
                    DuelsGame.endGame(victim, attacker, true);
                }
            }
        }


    }
}