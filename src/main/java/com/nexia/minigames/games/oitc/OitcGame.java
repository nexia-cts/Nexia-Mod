package com.nexia.minigames.games.oitc;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.Main;
import com.nexia.minigames.games.oitc.util.OitcScoreboard;
import com.nexia.minigames.games.oitc.util.player.PlayerData;
import com.nexia.minigames.games.oitc.util.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;

public class OitcGame {



    public static ArrayList<ServerPlayer> alive = new ArrayList<>();

    public static ArrayList<ServerPlayer> spectator = new ArrayList<>();

    public static ArrayList<ServerPlayer> deathPlayers = new ArrayList<>();

    public static String mapName = "city";

    // Both timers counted in seconds.
    public static int gameTime = 300;

    public static int queueTime = 30;

    public static ArrayList<ServerPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;


    public static void leave(ServerPlayer minecraftPlayer) {
        death(minecraftPlayer, minecraftPlayer.getLastDamageSource());
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);
        OitcGame.spectator.remove(minecraftPlayer);
        data.isSpectating = false;
        OitcScoreboard.removeScoreboardFor(minecraftPlayer);
        data.kills = 0;

        player.removeTag("in_oitc_game");

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        player.removeTag("oitc");
        OitcGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());

        data.gameMode = OitcGameMode.LOBBY;
    }

    public static void second() {
        if(OitcGame.isStarted) {
            OitcScoreboard.updateScoreboard();
            if(OitcGame.gameTime-- <= 0){
                stopGame();
            } else {
                OitcGame.gameTime--;
            }
        }

        if(!OitcGame.isStarted && OitcGame.queue.size() >= 2){
            if(OitcGame.queueTime-- <= 0){
                startGame();
            } else {
                OitcGame.queueTime--;
            }
            for(ServerPlayer player : OitcGame.queue){
                PlayerUtil.sendActionbar(player, "Time: " + OitcGame.queueTime);
            }
        }

        for(ServerPlayer player : OitcGame.deathPlayers){
            PlayerData data = PlayerDataManager.get(player);
            if(data.isDeathTime && data.deathTime++ <= 0){
                data.deathTime = 5;
                data.isDeathTime = false;
                spawnInRandomPos(player);
                player.setGameMode(GameType.ADVENTURE);
            }
        }
    }

    public static void joinQueue(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);
        data.isDeathTime = false;
        data.deathTime = 5;
        data.kills = 0;
        player.setHealth(20f);
        if(OitcGame.isStarted){
            OitcGame.spectator.add(player);
            data.isSpectating = true;
            PlayerDataManager.get(player).gameMode = OitcGameMode.SPECTATOR;
        } else {
            OitcGame.queue.add(player);
            data.isSpectating = false;
            PlayerUtil.broadcast(OitcGame.queue, LegacyChatFormat.format("{b2}{} {b1}has joined the game.", player.getScoreboardName()));
        }

        ServerLevel world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("oitc", OitcGame.mapName), null).asWorld();
        player.teleportTo(world, 0, 100, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void startGame() {
        if(OitcGame.queueTime <= 0){
            OitcGame.isStarted = true;
            OitcGame.gameTime = 300;
            OitcGame.alive.addAll(OitcGame.queue);

            ServerLevel world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("oitc", OitcGame.mapName), null).asWorld();

            ItemStack sword = new ItemStack(Items.STONE_SWORD);
            sword.getOrCreateTag().putBoolean("Unbreakable", true);
            sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            ItemStack bow = new ItemStack(Items.BOW);
            bow.enchant(Enchantments.POWER_ARROWS, 255);
            bow.getOrCreateTag().putBoolean("Unbreakable", true);
            bow.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
            bow.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            for(ServerPlayer player : OitcGame.alive) {
                player.inventory.setItem(0, sword);
                player.inventory.setItem(1, bow);
                player.inventory.setItem(8, new ItemStack(Items.ARROW));
                BlockPos pos = new BlockPos(0, 80, 0);

                PlayerDataManager.get(player).gameMode = OitcGameMode.PLAYING;

                player.addTag("in_oitc_game");
                player.removeTag(LobbyUtil.NO_DAMAGE_TAG);

                player.teleportTo(world, pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                player.setRespawnPosition(world.dimension(), pos, 0, true, false);
            }

            OitcScoreboard.setUpScoreboard();

            OitcGame.spectator.clear();
            OitcGame.queue.clear();
        }
    }

    public static void stopGame() {
        if(OitcGame.queueTime <= 0){
            OitcGame.isStarted = false;
            OitcGame.gameTime = 0;

            for(ServerPlayer player : OitcGame.alive){
                OitcScoreboard.removeScoreboardFor(player);
                player.removeTag("in_oitc_game");
                LobbyUtil.sendGame(player, "oitc", false, true);
            }

            OitcGame.alive.clear();
            OitcGame.spectator.clear();
            OitcGame.queue.clear();

            OitcGame.mapName = Main.config.oitcMaps.get(RandomUtil.randomInt(0, Main.config.oitcMaps.size()));
        }
    }

    public static void spawnInRandomPos(ServerPlayer player){
        String map = OitcGame.mapName;
    }

    public static boolean isOITCPlayer(ServerPlayer player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC;
    }

    public static void death(ServerPlayer victim, DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(!victimData.isDeathTime && !OitcGame.deathPlayers.contains(victim)) {
            ServerPlayer attacker = null;
            try {
                attacker = (ServerPlayer) source.getEntity(); 
            } catch(Exception ignored){
                try {
                    attacker = (ServerPlayer) victim.getLastDamageSource().getEntity();
                } catch (Exception ignored2) {
                }
            }

            if(attacker != null){
                PlayerData attackerData = PlayerDataManager.get(attacker);
                attackerData.kills++;
                attackerData.savedData.kills++;
                attacker.setHealth(20f);
                attacker.addItem(new ItemStack(Items.ARROW));
            }

            OitcGame.deathPlayers.add(victim);

            victimData.deathTime = 5;
            victimData.isDeathTime = true;
        }
    }

    public static void firstTick(MinecraftServer server){
        OitcSpawn.setOitcWorld(server);

        queue.clear();
        alive.clear();
        spectator.clear();
        deathPlayers.clear();

        mapName = "city";

        isStarted = false;
        queueTime = 30;
        gameTime = 300;
    }
}

