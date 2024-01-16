package com.nexia.minigames.games.oitc;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.oitc.util.player.PlayerData;
import com.nexia.minigames.games.oitc.util.player.PlayerDataManager;
import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.TickUtil;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class OitcGame {
    public static ArrayList<AccuratePlayer> players = new ArrayList<>();

    public static ArrayList<AccuratePlayer> spectator = new ArrayList<>();

    public static HashMap<AccuratePlayer, Integer> deathPlayers = new HashMap<>();

    public static ServerLevel world = null;

    public static OitcMap map = OitcMap.JUNGLE_PLAZA;


    // Both timers counted in seconds.
    public static int gameTime = 300;

    public static int queueTime = 15;

    public static ArrayList<AccuratePlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;

    private static AccuratePlayer winner = null;

    private static int endTime = 5;


    public static void leave(ServerPlayer minecraftPlayer) {
        OitcGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        AccuratePlayer accuratePlayer = AccuratePlayer.create(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);
        OitcGame.spectator.remove(accuratePlayer);
        OitcGame.queue.remove(accuratePlayer);
        OitcGame.players.remove(accuratePlayer);
        OitcGame.deathPlayers.remove(accuratePlayer);

        data.kills = 0;

        if(OitcGame.players.size() <= 1 && !OitcGame.isEnding) {
            if(OitcGame.players.size() == 1) OitcGame.endGame(OitcGame.players.get(0).get());
            else OitcGame.endGame(null);
        }

        player.removeTag("in_oitc_game");

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        if(data.gameMode.equals(OitcGameMode.PLAYING) && winner != accuratePlayer) {
            data.savedData.loss++;
        }

        player.removeTag("oitc");

        data.gameMode = OitcGameMode.LOBBY;
    }

    public static void second() {
        if(OitcGame.isStarted) {
            if(OitcGame.isEnding) {
                int color = 244 * 65536 + 166 * 256 + 71;
                // r * 65536 + g * 256 + b;
                if(OitcGame.winner.get() != null) DuelGameHandler.winnerRockets(OitcGame.winner.get(), OitcGame.world, color);

                if(OitcGame.endTime <= 0) {
                    for(ServerPlayer player : OitcGame.getViewers()){
                        PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                    }

                    OitcGame.resetAll();
                }

                OitcGame.endTime--;
            } else {
                try {
                    OitcGame.deathPlayers.forEach(((player, integer) -> {
                        int newInt = integer - 1;

                        if(!player.get().isSpectator()) player.get().setGameMode(GameType.SPECTATOR);

                        /*
                        OitcGame.deathPlayers.remove(player);
                        OitcGame.deathPlayers.put(player, newInt);
                        */

                        OitcGame.deathPlayers.replace(player, newInt);

                        Title title = getTitle(newInt);

                        PlayerUtil.getFactoryPlayer(player.get()).sendTitle(title);
                        PlayerUtil.sendSound(player.get(), new EntityPos(player.get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);


                        if(newInt <= 1){
                            spawnInRandomPos(player.get());
                            OitcGame.deathPlayers.remove(player);
                            //ServerTime.factoryServer.runCommand("/gamemode adventure " + player.get().getScoreboardName(), 4, false);
                            player.get().setGameMode(GameType.ADVENTURE);
                            BlfScheduler.delay(5, new BlfRunnable() {
                                @Override
                                public void run() {
                                    giveKit(player.get());
                                    player.get().setGameMode(GameType.ADVENTURE);
                                }
                            });

                        }
                    }));
                } catch (Exception ignored) { }
                OitcGame.updateInfo();

                if(OitcGame.gameTime <= 0 && !OitcGame.isEnding){

                    // Yes, I know I am a dumbass.
                    List<Integer> intKills = new ArrayList<>();
                    HashMap<Integer, ServerPlayer> kills = new HashMap<>();

                    for(AccuratePlayer player : OitcGame.players) {
                        intKills.add(PlayerDataManager.get(player.get()).kills);
                        kills.put(PlayerDataManager.get(player.get()).kills, player.get());
                    }

                    endGame(kills.get(Collections.max(intKills)));
                } else if(OitcGame.gameTime > 0 && !OitcGame.isEnding){
                    OitcGame.gameTime--;
                }
            }


        } else {
            if(OitcGame.queue.size() >= 2) {
                for(AccuratePlayer player : OitcGame.queue){
                    Player fPlayer = PlayerUtil.getFactoryPlayer(player.get());

                    if(OitcGame.queueTime <= 5) {
                        Title title = getTitle(OitcGame.queueTime);

                        PlayerUtil.getFactoryPlayer(player.get()).sendTitle(title);
                        PlayerUtil.sendSound(player.get(), new EntityPos(player.get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    fPlayer.sendActionBarMessage(
                            Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                                    .append(Component.text(OitcGame.map.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                    .append(Component.text(" (" + OitcGame.queue.size() + "/" + OitcGame.map.maxPlayers + ")").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(OitcGame.queueTime).color(ChatFormat.brandColor2))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Teaming is not allowed!").color(ChatFormat.failColor))
                    );

                    if(OitcGame.queueTime <= 5 || OitcGame.queueTime == 10 || OitcGame.queueTime == 15) {
                        fPlayer.sendMessage(Component.text("The game will start in ").color(TextColor.fromHexString("#b3b3b3"))
                                .append(Component.text(OitcGame.queueTime).color(ChatFormat.brandColor1))
                                .append(Component.text(" seconds.").color(TextColor.fromHexString("#b3b3b3")))
                        );
                    }
                }

                OitcGame.queueTime--;
            } else {
                OitcGame.queueTime = 15;
            }
            if(OitcGame.queueTime <= 0) startGame();
        }
    }

    @NotNull
    private static Title getTitle(int queueTime) {
        TextColor color = NamedTextColor.GREEN;

        if (queueTime <= 3 && queueTime > 1) {
            color = NamedTextColor.YELLOW;
        } else if (queueTime <= 1) {
            color = NamedTextColor.RED;
        }

        return Title.title(Component.text(queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
    }

    public static void joinQueue(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);
        data.kills = 0;
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        accuratePlayer.get().setHealth(accuratePlayer.get().getMaxHealth());
        if(OitcGame.isStarted || OitcGame.queue.size() >= OitcGame.map.maxPlayers){
            OitcGame.spectator.add(accuratePlayer);
            PlayerDataManager.get(player).gameMode = OitcGameMode.SPECTATOR;
            accuratePlayer.get().setGameMode(GameType.SPECTATOR);
        } else {
            OitcGame.queue.add(accuratePlayer);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.teleportTo(world, 0, 101, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void endGame(ServerPlayer serverPlayer) {
        OitcGame.isEnding = true;
        if(serverPlayer == null) return;
        PlayerData data = PlayerDataManager.get(serverPlayer);
        OitcGame.winner = AccuratePlayer.create(serverPlayer);

        PlayerUtil.getFactoryPlayer(serverPlayer).sendTitle(Title.title(Component.text("You won!").color(ChatFormat.greenColor), Component.text("")));

        data.savedData.wins++;

        for(ServerPlayer player : OitcGame.getViewers()){
            PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(Component.text(serverPlayer.getScoreboardName()).color(ChatFormat.brandColor2), Component.text("has won the game! (" + data.kills + " kills)").color(ChatFormat.normalColor)));
        }
    }

    public static void updateInfo() {
        String[] timer = TickUtil.minuteTimeStamp(OitcGame.gameTime * 20);
        for(ServerPlayer player : OitcGame.getViewers()) {
            PlayerUtil.getFactoryPlayer(player).sendActionBarMessage(
                    Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                            .append(Component.text(OitcGame.map.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(timer[0] + ":" + timer[1]).color(ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Kills » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(PlayerDataManager.get(player).kills).color(ChatFormat.brandColor2))
            );
        }
    }

    public static void giveKit(ServerPlayer player) {
        player.inventory.clearContent();

        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        ItemStack bow = new ItemStack(Items.BOW);
        ItemDisplayUtil.addGlint(bow);
        bow.getOrCreateTag().putBoolean("Unbreakable", true);
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
            OitcGame.players.addAll(OitcGame.queue);

            ItemStack sword = new ItemStack(Items.STONE_SWORD);
            sword.getOrCreateTag().putBoolean("Unbreakable", true);
            sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            ItemStack bow = new ItemStack(Items.BOW);
            ItemDisplayUtil.addGlint(bow);
            bow.getOrCreateTag().putBoolean("Unbreakable", true);
            bow.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            for(AccuratePlayer player : OitcGame.players) {
                ServerPlayer serverPlayer = player.get();
                serverPlayer.inventory.setItem(0, sword);
                serverPlayer.inventory.setItem(1, bow);
                serverPlayer.inventory.setItem(2, new ItemStack(Items.ARROW));

                PlayerDataManager.get(serverPlayer).gameMode = OitcGameMode.PLAYING;

                serverPlayer.addTag("in_oitc_game");
                serverPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);

                spawnInRandomPos(serverPlayer);

                //player.setRespawnPosition(world.dimension(), pos, 0, true, false);
            }

            OitcGame.spectator.clear();
            OitcGame.queue.clear();
        }
    }

    public static void spawnInRandomPos(ServerPlayer player){
        OitcMap map = OitcGame.map;
        EntityPos spawnPosition = map.spawnPositions.get(RandomUtil.randomInt(map.spawnPositions.size()));

        spawnPosition.teleportPlayer(OitcGame.world, player);
    }

    public static boolean isOITCPlayer(net.minecraft.world.entity.player.Player player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC || player.getTags().contains("oitc") || player.getTags().contains("in_oitc_game");
    }

    public static void death(ServerPlayer victim, DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        AccuratePlayer accurateVictim = AccuratePlayer.create(victim);
        if(OitcGame.isStarted && !OitcGame.deathPlayers.containsKey(accurateVictim) && victimData.gameMode == OitcGameMode.PLAYING) {
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim);
            if(attacker != null && victim != attacker) {
                PlayerData attackerData = PlayerDataManager.get(attacker);
                attackerData.kills++;
                attackerData.savedData.kills++;
                attacker.setHealth(attacker.getMaxHealth());
                attacker.addItem(new ItemStack(Items.ARROW));
            }

            PlayerDataManager.get(victim).hasDied = true;
            OitcGame.deathPlayers.remove(accurateVictim);
            OitcGame.deathPlayers.put(accurateVictim, 6);
        }
    }

    public static void resetAll() {
        queue.clear();
        players.clear();
        spectator.clear();
        deathPlayers.clear();

        map = OitcMap.oitcMaps.get(RandomUtil.randomInt(OitcMap.oitcMaps.size()));

        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("oitc", OitcGame.map.id), new RuntimeWorldConfig()).asWorld();

        isStarted = false;
        queueTime = 15;
        gameTime = 300;
        isEnding = false;
        winner = null;
        endTime = 5;
    }

    public static void tick() {

        AABB aabb = new AABB(OitcGame.map.corner1, OitcGame.map.corner2);
        Predicate<Entity> predicate = o -> true;

        for (ItemEntity entity : OitcGame.world.getEntities(EntityType.ITEM, aabb, predicate)) {
            // kill @e[type=item,distance=0..]
            entity.remove();
        }

        if(!OitcGame.isStarted) return;

        for (Arrow entity : OitcGame.world.getEntities(EntityType.ARROW, aabb, predicate)) {
            // kill @e[type=item,distance=0..]

            OitcGame.world.addParticle(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 0.0, 0.0, 0.0);
            if(entity.inGround) {
                entity.remove();
            }
        }
    }

    public static void firstTick(){
        resetAll();
    }

    public static ArrayList<ServerPlayer> getViewers() {
        ArrayList<ServerPlayer> viewers = new ArrayList<>();
        OitcGame.players.forEach(player -> viewers.add(player.get()));
        OitcGame.spectator.forEach(player -> viewers.add(player.get()));
        return viewers;
    }
}
