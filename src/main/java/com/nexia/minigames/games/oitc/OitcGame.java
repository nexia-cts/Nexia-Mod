package com.nexia.minigames.games.oitc;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
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
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.ArrayList;

public class OitcGame {
    public static ArrayList<ServerPlayer> alive = new ArrayList<>();

    public static ArrayList<ServerPlayer> spectator = new ArrayList<>();

    public static ArrayList<ServerPlayer> deathPlayers = new ArrayList<>();

    public static ServerLevel world = null;

    public static OitcMap map = OitcMap.CITY;

    public static ArrayList<int[]> spawnPositions = new ArrayList<>();

    // Both timers counted in seconds.
    public static int gameTime = 300;

    public static int queueTime = 15;

    public static ArrayList<ServerPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;


    public static void leave(ServerPlayer minecraftPlayer) {
        OitcGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);
        OitcGame.spectator.remove(minecraftPlayer);
        OitcGame.queue.remove(minecraftPlayer);
        OitcGame.alive.remove(minecraftPlayer);
        OitcGame.deathPlayers.remove(minecraftPlayer);

        OitcScoreboard.removeScoreboardFor(minecraftPlayer);
        data.kills = 0;
        data.deathTime = 5;

        player.removeTag("in_oitc_game");

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        player.removeTag("oitc");

        data.gameMode = OitcGameMode.LOBBY;
    }

    public static void second() {
        if(OitcGame.isStarted) {
            OitcScoreboard.updateScoreboard();
            OitcGame.gameTime--;
            if(OitcGame.gameTime <= 0){
                stopGame();
            }
        } else {
            if(OitcGame.queue.size() >= 2) {
                PlayerUtil.broadcast(OitcGame.queue, "§7The game will start in §5" + OitcGame.queueTime + " §7seconds.");
                OitcGame.queueTime--;
            } else {
                OitcGame.queueTime = 15;
            }
            if(OitcGame.queueTime <= 0){
                startGame();
            }
            for(ServerPlayer player : OitcGame.queue){
                PlayerUtil.sendActionbar(player, "§7Time: §f" + OitcGame.queueTime);
            }
        }

        for(ServerPlayer player : OitcGame.deathPlayers){
            PlayerData data = PlayerDataManager.get(player);
            data.deathTime--;
            if(data.deathTime <= 0){
                data.deathTime = 5;
                spawnInRandomPos(player);
                giveKit(player);
                player.setGameMode(GameType.ADVENTURE);
            }
        }
    }

    public static void joinQueue(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);
        data.deathTime = 5;
        data.kills = 0;
        player.setHealth(player.getMaxHealth());
        if(OitcGame.isStarted){
            OitcGame.spectator.add(player);
            PlayerDataManager.get(player).gameMode = OitcGameMode.SPECTATOR;
            player.setGameMode(GameType.SPECTATOR);
        } else {
            OitcGame.queue.add(player);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }


        player.teleportTo(world, 0, 101, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void giveKit(ServerPlayer player) {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 1000);
        bow.getOrCreateTag().putBoolean("Unbreakable", true);
        //bow.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        // test why one shot isnt working
        bow.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        ItemStack arrow = new ItemStack(Items.ARROW);

        player.setSlot(0, sword);
        player.setSlot(1, bow);
        player.setSlot(2, arrow);
    }

    public static void startGame() {
        if(OitcGame.queueTime <= 0){
            OitcGame.isStarted = true;
            OitcGame.gameTime = 300;
            OitcGame.alive.addAll(OitcGame.queue);

            ItemStack sword = new ItemStack(Items.STONE_SWORD);
            sword.getOrCreateTag().putBoolean("Unbreakable", true);
            sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            ItemStack bow = new ItemStack(Items.BOW);
            bow.enchant(Enchantments.POWER_ARROWS, 1000);
            bow.getOrCreateTag().putBoolean("Unbreakable", true);
            //bow.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
            // test why one shot isnt working
            bow.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            for(ServerPlayer player : OitcGame.alive) {
                player.inventory.setItem(0, sword);
                player.inventory.setItem(1, bow);
                player.inventory.setItem(2, new ItemStack(Items.ARROW));

                PlayerDataManager.get(player).gameMode = OitcGameMode.PLAYING;

                player.addTag("in_oitc_game");
                player.removeTag(LobbyUtil.NO_DAMAGE_TAG);

                spawnInRandomPos(player);

                //player.setRespawnPosition(world.dimension(), pos, 0, true, false);
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
                PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
            }

            OitcGame.alive.clear();
            OitcGame.spectator.clear();
            OitcGame.queue.clear();

            OitcGame.map.id = OitcMap.stringOitcMaps.get(RandomUtil.randomInt(0, OitcMap.stringOitcMaps.size()));
            world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("oitc", OitcGame.map.id), new RuntimeWorldConfig()).asWorld();
        }
    }

    public static void spawnInRandomPos(ServerPlayer player){
        String map = OitcGame.map.id;
        int[] pos = OitcGame.spawnPositions.get(RandomUtil.randomInt(0, OitcGame.spawnPositions.size()));

        player.teleportTo(world, pos[0], pos[1], pos[2], pos[3], pos[4]);
    }

    public static boolean isOITCPlayer(net.minecraft.world.entity.player.Player player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC || player.getTags().contains("oitc");
    }

    public static void death(ServerPlayer victim, DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(!OitcGame.deathPlayers.contains(victim) && OitcGame.isStarted && OitcGame.alive.contains(victim) && victimData.gameMode == OitcGameMode.PLAYING) {
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim);

            if(attacker != null){
                PlayerData attackerData = PlayerDataManager.get(attacker);
                attackerData.kills++;
                attackerData.savedData.kills++;
                attacker.setHealth(attacker.getMaxHealth());
                attacker.addItem(new ItemStack(Items.ARROW));
            }

            OitcGame.deathPlayers.add(victim);
            victimData.deathTime = 5;
        }
    }

    public static void firstTick(MinecraftServer server){
        queue.clear();
        alive.clear();
        spectator.clear();
        deathPlayers.clear();

        map = OitcMap.CITY; // Placeholder Map
        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("oitc", OitcGame.map.id), new RuntimeWorldConfig()).asWorld();

        isStarted = false;
        queueTime = 15;
        gameTime = 300;
    }

    static {
        spawnPositions.add(new int[]{0, 80, 0, 0, 0});
    }
}
