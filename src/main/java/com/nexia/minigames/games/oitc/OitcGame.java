package com.nexia.minigames.games.oitc;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.TickUtil;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.oitc.util.player.OITCPlayerData;
import com.nexia.nexus.api.world.types.Minecraft;
import net.kyori.adventure.text.Component;
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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class OitcGame {
    public static final ResourceLocation OITC_DATA_MANAGER = NexiaCore.id("oitc");
    public static ArrayList<NexiaPlayer> players = new ArrayList<>();

    public static ArrayList<NexiaPlayer> spectator = new ArrayList<>();

    public static HashMap<NexiaPlayer, Integer> deathPlayers = new HashMap<>();

    public static ServerLevel world = null;

    public static OitcMap map = OitcMap.JUNGLE_PLAZA;


    // Both timers counted in seconds.
    public static int gameTime = 300;

    public static int queueTime = 15;

    public static ArrayList<NexiaPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;

    private static NexiaPlayer winner = null;

    public static final String OITC_TAG = "oitc";

    private static int endTime = 5;


    public static void leave(NexiaPlayer player) {
        OitcGame.death(player, player.unwrap().getLastDamageSource());

        OITCPlayerData data = (OITCPlayerData) PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player);
        OitcGame.spectator.remove(player);
        OitcGame.queue.remove(player);
        OitcGame.players.remove(player);
        OitcGame.deathPlayers.remove(player);

        data.kills = 0;

        if(OitcGame.players.size() <= 1 && !OitcGame.isEnding && OitcGame.isStarted) {
            if(OitcGame.players.size() == 1) OitcGame.endGame(OitcGame.players.getFirst());
            else OitcGame.endGame(null);
        }

        player.removeTag("in_oitc_game");

        player.reset(true, Minecraft.GameMode.ADVENTURE);

        if(data.gameMode.equals(OitcGameMode.PLAYING) && winner != player) {
            data.savedData.incrementInteger("losses");
        }

        player.removeTag("oitc");

        data.gameMode = OitcGameMode.LOBBY;
    }

    public static void second() {
        if(OitcGame.isStarted) {
            if(OitcGame.isEnding) {
                int color = 244 * 65536 + 166 * 256 + 71;
                // r * 65536 + g * 256 + b;
                if(OitcGame.winner != null && OitcGame.winner.unwrap() != null) DuelGameHandler.winnerRockets(OitcGame.winner, OitcGame.world, color);

                if(OitcGame.endTime <= 0) {
                    for(NexiaPlayer player : OitcGame.getViewers()){
                        player.runCommand("/hub", 0, false);
                    }

                    OitcGame.resetAll();
                }

                OitcGame.endTime--;
            } else {
                try {
                    OitcGame.deathPlayers.forEach(((player, integer) -> {
                        int newInt = integer - 1;

                        if(!player.getGameMode().equals(Minecraft.GameMode.SPECTATOR)) player.setGameMode(Minecraft.GameMode.SPECTATOR);

                        /*
                        OitcGame.deathPlayers.remove(player);
                        OitcGame.deathPlayers.put(player, newInt);
                        */

                        OitcGame.deathPlayers.replace(player, newInt);

                        Title title = getTitle(newInt);

                        player.sendTitle(title);
                        player.sendSound(new EntityPos(player), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);


                        if(newInt <= 1){
                            spawnInRandomPos(player);
                            OitcGame.deathPlayers.remove(player);
                            //ServerTime.nexusServer.runCommand("/gamemode adventure " + player.get().getScoreboardName(), 4, false);
                            player.setGameMode(Minecraft.GameMode.ADVENTURE);
                            ServerTime.scheduler.schedule(() -> {
                                giveKit(player);
                                player.setGameMode(Minecraft.GameMode.ADVENTURE);
                            }, 5);

                        }
                    }));
                } catch (Exception ignored) { }
                OitcGame.updateInfo();

                if(OitcGame.gameTime <= 0 && !OitcGame.isEnding && OitcGame.isStarted){

                    // Yes, I know I am a dumbass.
                    List<Integer> intKills = new ArrayList<>();
                    HashMap<Integer, NexiaPlayer> kills = new HashMap<>();

                    for(NexiaPlayer player : OitcGame.players) {
                        intKills.add(((OITCPlayerData)PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player)).kills);
                        kills.put(((OITCPlayerData)PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player)).kills, player);
                    }

                    endGame(kills.get(Collections.max(intKills)));
                } else if(OitcGame.gameTime > 0 && !OitcGame.isEnding){
                    OitcGame.gameTime--;
                }
            }


        } else {
            if(OitcGame.queue.size() >= 2) {
                for(NexiaPlayer player : OitcGame.queue){
                    if(OitcGame.queueTime <= 5) {
                        Title title = getTitle(OitcGame.queueTime);

                        player.sendTitle(title);
                        player.sendSound(new EntityPos(player.unwrap()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    player.sendActionBarMessage(
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
                        player.sendMessage(Component.text("The game will start in ").color(TextColor.fromHexString("#b3b3b3"))
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
        TextColor color = ChatFormat.Minecraft.green;

        if (queueTime == 2) {
            color = ChatFormat.Minecraft.yellow;
        } else if (queueTime <= 1) {
            color = ChatFormat.Minecraft.red;
        }

        return Title.title(Component.text(queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
    }

    public static void joinQueue(NexiaPlayer player) {
        OITCPlayerData data = (OITCPlayerData) PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player);
        data.kills = 0;
        player.setHealth(player.unwrap().getMaxHealth());
        if(OitcGame.isStarted || OitcGame.queue.size() >= OitcGame.map.maxPlayers){
            OitcGame.spectator.add(player);
            ((OITCPlayerData)PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player)).gameMode = OitcGameMode.SPECTATOR;
            player.setGameMode(Minecraft.GameMode.SPECTATOR);
        } else {
            OitcGame.queue.add(player);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.unwrap().teleportTo(world, 0, 101, 0, 0, 0);
        player.unwrap().setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void endGame(NexiaPlayer player) {
        OitcGame.isEnding = true;
        if(player == null || player.unwrap() == null) return;
        OITCPlayerData data = (OITCPlayerData) PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player);
        OitcGame.winner = player;

        player.sendTitle(Title.title(Component.text("You won!").color(ChatFormat.greenColor), Component.text("")));

        data.savedData.incrementInteger("wins");

        for(NexiaPlayer viewer : OitcGame.getViewers()){
            viewer.sendTitle(Title.title(Component.text(player.getRawName()).color(ChatFormat.brandColor2), Component.text("has won the game! (" + data.kills + " kills)").color(ChatFormat.normalColor)));
        }
    }

    public static void updateInfo() {
        String[] timer = TickUtil.minuteTimeStamp(OitcGame.gameTime * 20);
        for(NexiaPlayer player : OitcGame.getViewers()) {
            player.sendActionBarMessage(
                    Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                            .append(Component.text(OitcGame.map.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(timer[0] + ":" + timer[1]).color(ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Kills » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(((OITCPlayerData)PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player)).kills).color(ChatFormat.brandColor2))
            );
        }
    }

    public static void giveKit(NexiaPlayer player) {
        player.getInventory().clear();

        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        ItemStack bow = new ItemStack(Items.BOW);
        ItemDisplayUtil.addGlint(bow);
        bow.getOrCreateTag().putBoolean("Unbreakable", true);
        bow.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        ItemStack arrow = new ItemStack(Items.ARROW);

        player.unwrap().setSlot(0, sword);
        player.unwrap().setSlot(1, bow);
        player.unwrap().setSlot(2, arrow);
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

            for(NexiaPlayer player : OitcGame.players) {
                player.unwrap().inventory.setItem(0, sword);
                player.unwrap().inventory.setItem(1, bow);
                player.unwrap().inventory.setItem(2, new ItemStack(Items.ARROW));

                ((OITCPlayerData)PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(player)).gameMode = OitcGameMode.PLAYING;

                player.addTag("in_oitc_game");
                player.removeTag(LobbyUtil.NO_DAMAGE_TAG);

                spawnInRandomPos(player);

                //player.setRespawnPosition(world.dimension(), pos, 0, true, false);
            }

            OitcGame.spectator.clear();
            OitcGame.queue.clear();
        }
    }

    public static void spawnInRandomPos(NexiaPlayer player){
        OitcMap map = OitcGame.map;
        EntityPos spawnPosition = map.spawnPositions.get(RandomUtil.randomInt(map.spawnPositions.size()));

        spawnPosition.teleportPlayer(OitcGame.world, player.unwrap());
    }

    public static boolean isOITCPlayer(NexiaPlayer player){
        return ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode == PlayerGameMode.OITC || player.hasTag("oitc") || player.hasTag("in_oitc_game");
    }

    public static void death(NexiaPlayer victim, DamageSource source){
        OITCPlayerData victimData = (OITCPlayerData) PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(victim);
        if(OitcGame.isStarted && !OitcGame.deathPlayers.containsKey(victim) && victimData.gameMode == OitcGameMode.PLAYING) {
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim.unwrap());
            if(attacker != null) {
                NexiaPlayer nexiaAttacker = new NexiaPlayer(attacker);
                if(!nexiaAttacker.equals(victim)) {
                    OITCPlayerData attackerData = (OITCPlayerData) PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(nexiaAttacker);
                    attackerData.kills++;
                    attackerData.savedData.incrementInteger("kills");
                    attacker.setHealth(attacker.getMaxHealth());
                    attacker.addItem(new ItemStack(Items.ARROW));
                }
            }

            ((OITCPlayerData)PlayerDataManager.getDataManager(OITC_DATA_MANAGER).get(victim)).hasDied = true;
            OitcGame.deathPlayers.remove(victim);
            OitcGame.deathPlayers.put(victim, 4); // 3 seconds
        }
    }

    public static void resetAll() {
        queue.clear();
        players.clear();
        spectator.clear();
        deathPlayers.clear();

        map = OitcMap.oitcMaps.get(RandomUtil.randomInt(OitcMap.oitcMaps.size()));

        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("oitc", OitcGame.map.id), WorldUtil.defaultWorldConfig).asWorld();

        isStarted = false;
        queueTime = 15;
        gameTime = 300;
        isEnding = false;
        winner = null;
        endTime = 5;
    }

    public static void tick() {
        if(OitcGame.world == null) return;
        if(OitcGame.world.players().isEmpty()) return;

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

    public static ArrayList<NexiaPlayer> getViewers() {
        ArrayList<NexiaPlayer> viewers = new ArrayList<>();
        viewers.addAll(OitcGame.players);
        viewers.addAll(OitcGame.spectator);
        return viewers;
    }
}
