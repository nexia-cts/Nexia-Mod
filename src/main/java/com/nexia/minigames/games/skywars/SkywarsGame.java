package com.nexia.minigames.games.skywars;

import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.skywars.util.player.PlayerData;
import com.nexia.minigames.games.skywars.util.player.PlayerDataManager;
import net.fabricmc.loader.impl.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.TickUtil;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import static com.nexia.core.utilities.world.WorldUtil.getChunkGenerator;

public class SkywarsGame {
    public static ArrayList<NexiaPlayer> alive = new ArrayList<>();

    public static ArrayList<NexiaPlayer> spectator = new ArrayList<>();

    public static ServerLevel world = null;

    public static SkywarsMap map = SkywarsMap.PLACEHOLDER;

    public static RuntimeWorldConfig config = new RuntimeWorldConfig()
            .setDimensionType(DimensionType.OVERWORLD_LOCATION)
            .setGenerator(getChunkGenerator())
            .setDifficulty(Difficulty.EASY)
            .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
            .setGameRule(GameRules.RULE_MOBGRIEFING, true)
            .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
            .setGameRule(GameRules.RULE_FALL_DAMAGE, true)
            .setGameRule(GameRules.RULE_DAYLIGHT, false)
            .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
            .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
            .setGameRule(GameRules.RULE_NATURAL_REGENERATION, true)
            .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
            .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0)
            .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
            .setTimeOfDay(6000);

    // Both timers counted in seconds.
    public static int glowingTime = 180;

    public static String id = UUID.randomUUID().toString();
    public static int gameEnd = 360;
    public static int queueTime = 15;

    public static ArrayList<NexiaPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;
    public static boolean isGlowingActive = false;

    private static NexiaPlayer winner = null;

    public static final String SKYWARS_TAG = "skywars";

    private static int endTime = 5;

    public static CustomBossEvent BOSSBAR = ServerTime.minecraftServer.getCustomBossEvents().get(new ResourceLocation("skywars", "timer"));

    public static void leave(ServerPlayer minecraftPlayer) {
        AccuratePlayer accuratePlayer = AccuratePlayer.create(minecraftPlayer);
        SkywarsGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(player);
        SkywarsGame.spectator.remove(player);

        if(!SkywarsGame.isStarted && SkywarsGame.queue.contains(player)) {
            SkywarsGame.map = SkywarsMap.calculateMap(SkywarsGame.queue.size(), true);
        }
        SkywarsGame.queue.remove(player);
        SkywarsGame.alive.remove(player);

        data.kills = 0;

        PlayerUtil.resetHealthStatus(accuratePlayer.get());
        accuratePlayer.get().setGameMode(GameType.ADVENTURE);

        accuratePlayer.get().inventory.clearContent();
        accuratePlayer.get().setExperienceLevels(0);
        accuratePlayer.get().setExperiencePoints(0);
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        player.removeTag(SKYWARS_TAG);

        data.gameMode = SkywarsGameMode.LOBBY;
    }

    public static void second() {
        if(SkywarsGame.isStarted) {

            if(SkywarsGame.isEnding) {
                int color = 160 * 65536 + 248;
                // r * 65536 + g * 256 + b;
                if(SkywarsGame.winner.get() == null) SkywarsGame.endTime = 0;
                else DuelGameHandler.winnerRockets(SkywarsGame.winner.get(), SkywarsGame.world, color);


                if(SkywarsGame.endTime <= 0) {
                    for(ServerPlayer player : SkywarsGame.getViewers()){
                        PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                    }

                    SkywarsGame.resetAll();
                }

                SkywarsGame.endTime--;
            } else {
                SkywarsGame.updateInfo();

                if(SkywarsGame.glowingTime <= 0 && !SkywarsGame.isGlowingActive && !SkywarsGame.isEnding){
                    SkywarsGame.glowPlayers();
                } else if(SkywarsGame.glowingTime > 0 && !SkywarsGame.isGlowingActive && !SkywarsGame.isEnding){
                    SkywarsGame.glowingTime--;
                }

                if(SkywarsGame.gameEnd > 0 && !SkywarsGame.isEnding) {
                    SkywarsGame.gameEnd--;
                }

                if(SkywarsGame.gameEnd == 60 && !SkywarsGame.isEnding) SkywarsGame.sendCenterWarning();
                if(SkywarsGame.gameEnd <= 0 && !SkywarsGame.isEnding) SkywarsGame.winNearestCenter();
            }


        } else {
            if(SkywarsGame.queue.size() >= 2) {
                for(NexiaPlayer player : SkywarsGame.queue){
                    if(SkywarsGame.queueTime <= 5) {
                        fPlayer.sendTitle(getTitle());
                        PlayerUtil.sendSound(player.get(), new EntityPos(player.get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    player.sendActionBarMessage(
                            Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                                    .append(Component.text(StringUtil.capitalize(SkywarsGame.map.id)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                    .append(Component.text(" (" + SkywarsGame.queue.size() + "/" + SkywarsMap.maxJoinablePlayers + ")").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(SkywarsGame.queueTime).color(ChatFormat.brandColor2))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Teaming is not allowed!").color(ChatFormat.failColor))
                    );
                }
                if(SkywarsGame.queueTime <= 5 || SkywarsGame.queueTime == 10 || SkywarsGame.queueTime == 15) {
                    for(NexiaPlayer queuePlayers : SkywarsGame.queue) {
                        queuePlayers.sendMessage(Component.text("The game will start in ", ChatFormat.systemColor)
                                .append(Component.text(SkywarsGame.queueTime, ChatFormat.brandColor2))
                                .append(Component.text(" seconds.", ChatFormat.systemColor))
                        );
                    }
                }

                SkywarsGame.queueTime--;
            } else {
                SkywarsGame.queueTime = 15;
            }

            if(SkywarsGame.queueTime <= 0) startGame();
        }
    }

    @NotNull
    private static Title getTitle() {
        TextColor color = NamedTextColor.GREEN;

        if(SkywarsGame.queueTime <= 3 && SkywarsGame.queueTime > 1) {
            color = NamedTextColor.YELLOW;
        } else if(SkywarsGame.queueTime <= 1) {
            color = NamedTextColor.RED;
        }

        return Title.title(Component.text(SkywarsGame.queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
    }

    public static void joinQueue(NexiaPlayer player) {
        PlayerData data = PlayerDataManager.get(player);
        data.kills = 0;
        PlayerUtil.resetHealthStatus(player);


        if(SkywarsGame.isStarted || SkywarsGame.queue.size() >= SkywarsMap.maxJoinablePlayers){
            SkywarsGame.spectator.add(player);
            PlayerDataManager.get(player).gameMode = SkywarsGameMode.SPECTATOR;
            PlayerUtil.sendBossbar(SkywarsGame.BOSSBAR, player, false);
            player.setGameMode(GameType.SPECTATOR);
        } else {
            SkywarsGame.queue.add(AccuratePlayer.create(player));
            player.setGameMode(GameType.ADVENTURE);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);

            SkywarsGame.map = SkywarsMap.calculateMap(SkywarsGame.queue.size(), true);

        }

        player.teleportTo(world, 0, 128.1, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 128, 0), 0, true, false);
    }

    public static void resetMap() {
        //SkywarsMap.deleteWorld(SkywarsGame.id);

        SkywarsGame.id = UUID.randomUUID().toString();

        //SkywarsGame.map = SkywarsMap.skywarsMaps.get(RandomUtil.randomInt(SkywarsMap.skywarsMaps.size()));
        ServerLevel level = ServerTime.fantasy.openTemporaryWorld(
                SkywarsGame.config,
                new ResourceLocation("skywars", SkywarsGame.id)).asWorld();

        //SkywarsGame.map.structureMap.pasteMap(level);
        ServerTime.factoryServer.runCommand(String.format("execute in skywars:%s run worldborder set 200", SkywarsGame.id), 4, false);
        SkywarsMap.spawnQueueBuild(level, false);
        SkywarsGame.world = level;

        SkywarsGame.map = SkywarsMap.PLACEHOLDER;

        if(Main.config.debugMode) Main.logger.info(String.format("[DEBUG]: New Skywars Map (%s) has been reset (not pasted)", SkywarsGame.id));
    }

    public static void startGame() {

        SkywarsGame.map = SkywarsMap.validateMap(SkywarsGame.map, SkywarsGame.queue.size());

        if(SkywarsGame.queue.size() > SkywarsGame.map.maxPlayers) {
            while(SkywarsGame.queue.size() > SkywarsGame.map.maxPlayers) {
                NexiaPlayer player = SkywarsGame.queue.get(RandomUtil.randomInt(SkywarsGame.queue.size()));
                SkywarsGame.queue.remove(player);
                //LobbyUtil.returnToLobby(accuratePlayer.get(), true);
                ServerTime.minecraftServer.getCommands().performCommand(accuratePlayer.get().createCommandSourceStack(), "/hub");
            }
        }

        SkywarsGame.isStarted = true;
        SkywarsGame.isGlowingActive = false;
        SkywarsGame.isEnding = false;
        SkywarsGame.winner = null;
        SkywarsGame.glowingTime = 180;
        SkywarsGame.gameEnd = 360;
        SkywarsGame.alive.addAll(SkywarsGame.queue);

        SkywarsGame.map.structureMap.pasteMap(SkywarsGame.world);
        if(Main.config.debugMode) Main.logger.info(String.format("[DEBUG]: Skywars Map (%s) has been pasted on skywars:%s.", SkywarsGame.map.id, SkywarsGame.id));

        ArrayList<EntityPos> positions = new ArrayList<>(SkywarsGame.map.positions);

        for (NexiaPlayer player : SkywarsGame.alive) {
            EntityPos pos = positions.get(RandomUtil.randomInt(positions.size()));

            PlayerDataManager.get(serverPlayer).gameMode = SkywarsGameMode.PLAYING;
            serverPlayer.addTag(SKYWARS_TAG);
            serverPlayer.addTag(LobbyUtil.NO_SATURATION_TAG);
            serverPlayer.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            serverPlayer.setGameMode(GameType.SURVIVAL);
            pos.teleportPlayer(SkywarsGame.world, serverPlayer);

            positions.remove(pos);
        }

        SkywarsMap.spawnQueueBuild(SkywarsGame.world, true);

        SkywarsGame.spectator.clear();
        SkywarsGame.queue.clear();

        PlayerUtil.sendBossbar(SkywarsGame.BOSSBAR, SkywarsGame.getViewers());
    }

    public static void resetAll() {
        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Skywars Game has been reset.");
        SkywarsGame.isStarted = false;
        SkywarsGame.isGlowingActive = false;
        SkywarsGame.glowingTime = 180;
        SkywarsGame.isEnding = false;
        SkywarsGame.gameEnd = 360;
        SkywarsGame.endTime = 5;
        SkywarsGame.queueTime = 15;

        SkywarsGame.alive.clear();
        SkywarsGame.spectator.clear();
        SkywarsGame.queue.clear();

        SkywarsGame.resetMap();
    }

    public static void endGame(@NotNull AccuratePlayer accuratePlayer) {
        if(accuratePlayer.get() == null) return;
        if(Main.config.debugMode) Main.logger.info(String.format("[DEBUG]: Skywars Game (%s) is ending.", SkywarsGame.id));

        SkywarsGame.isEnding = true;

        SkywarsGame.winner = player;

        PlayerDataManager.get(player).savedData.wins++;

        for(ServerPlayer serverPlayer : SkywarsGame.getViewers()){
            PlayerUtil.getFactoryPlayer(serverPlayer).sendTitle(Title.title(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2), Component.text("has won the game!").color(ChatFormat.normalColor)
                    .append(Component.text(" [")
                            .color(ChatFormat.lineColor))
                    .append(Component.text(FfaUtil.calculateHealth(player.getHealth()) + "❤").color(ChatFormat.failColor))
                    .append(Component.text("]").color(ChatFormat.lineColor))
            ));
        }
    }

    public static void updateInfo() {
        CustomBossEvent bossbar = SkywarsGame.BOSSBAR;

        for(NexiaPlayer player : SkywarsGame.getViewers()) {
            player.sendActionBarMessage(
                    Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                            .append(Component.text(StringUtil.capitalize(SkywarsGame.map.id)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Players » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(SkywarsGame.alive.size()).color(ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Teaming is not allowed!").color(ChatFormat.failColor))
            );
        }

        if(!SkywarsGame.isGlowingActive) {
            String[] timer = TickUtil.minuteTimeStamp(glowingTime * 20);
            TextComponent updatedTime = new TextComponent("§7Glow in §a" + timer[0].substring(1) + "m, " + timer[1] + "s" + "§7...");

            bossbar.setValue(glowingTime);
            bossbar.setName(updatedTime);
            return;
        }

        if(SkywarsGame.gameEnd > 0) {
            String[] timer = TickUtil.minuteTimeStamp(gameEnd * 20);
            TextComponent updatedTime = new TextComponent("§7Game end in §a" + timer[0].substring(1) + "m, " + timer[1] + "s" + "§7...");

            bossbar.setValue(gameEnd);
            bossbar.setName(updatedTime);
            return;
        }
    }

    public static void glowPlayers() {
        SkywarsGame.isGlowingActive = true;
        SkywarsGame.alive.forEach((player) -> player.get().setGlowing(true));

        for(ServerPlayer player : SkywarsGame.getViewers()) {
            Player fPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerUtil.sendSound(player, new EntityPos(player.position()), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.AMBIENT, 1000, 1);
            fPlayer.sendMessage(
                    Component.text("[").color(ChatFormat.lineColor)
                            .append(Component.text("⚠").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            .append(Component.text(" All players have received glowing.").color(TextColor.fromHexString("#FFE588")))
            );

            player.sendTitle(
                    Title.title(Component.text("⚠").color(ChatFormat.failColor),
                            Component.text(" All players have received glowing.").color(TextColor.fromHexString("#FFE588")))
            );
        }
    }

    public static void sendCenterWarning() {
        for(ServerPlayer player : SkywarsGame.getViewers()) {
            Player fPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerUtil.sendSound(player, new EntityPos(player.position()), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.AMBIENT, 1000, 1);
            fPlayer.sendMessage(
                    Component.text("[").color(ChatFormat.lineColor)
                            .append(Component.text("⚠").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            .append(Component.text(" In 1 minute, the closest player to center will win.").color(TextColor.fromHexString("#FFE588")))
            );

            player.sendTitle(
                    Title.title(Component.text("⚠").color(ChatFormat.failColor),
                            Component.text(" In 1 minute, the closest player to center will win.").color(TextColor.fromHexString("#FFE588")))
            );
        }
    }

    public static void winNearestCenter() {
        if(SkywarsGame.isEnding) return;
        ServerPlayer closestPlayer = (ServerPlayer) SkywarsGame.world.getNearestPlayer(0, 80, 0, 1000, e -> e instanceof ServerPlayer se && !se.isCreative() && !se.isSpectator() && SkywarsGame.isSkywarsPlayer(se));

        assert closestPlayer != null;
        endGame(AccuratePlayer.create(closestPlayer));
    }

    public static boolean isSkywarsPlayer(net.minecraft.world.entity.player.Player player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.SKYWARS || player.getTags().contains("skywars");
    }

    public static void death(NexiaPlayer victim, DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(SkywarsGame.isStarted && SkywarsGame.alive.contains(accurateVictim) && victimData.gameMode == SkywarsGameMode.PLAYING) {
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker(victim);

            if(attacker != null){
                PlayerData attackerData = PlayerDataManager.get(attacker);
                attackerData.kills++;
                attackerData.savedData.kills++;
            }

            victim.destroyVanishingCursedItems();
            victim.inventory.dropAll();

            if(SkywarsGame.winner != victim) victimData.savedData.losses++;
            SkywarsGame.alive.remove(victim);
            SkywarsGame.spectator.add(victim);
            PlayerDataManager.get(victim).gameMode = SkywarsGameMode.SPECTATOR;

            String mainColor = LegacyChatFormat.chatColor2;
            String message = mainColor + victim.getCombatTracker().getDeathMessage().getString();

            for(NexiaPlayer nexiaPlayer : SkywarsGame.getViewers()) {
                nexiaPlayer.sendMessage(message);
            }

            if(SkywarsGame.alive.size() == 1 && !SkywarsGame.isEnding) {
                SkywarsGame.endGame(SkywarsGame.alive.getFirst());
            }
        }
    }

    public static void firstTick(){
        SkywarsGame.resetAll();
    }

    public static ArrayList<NexiaPlayer> getViewers() {
        ArrayList<NexiaPlayer> viewers = new ArrayList<>();
        viewers.addAll(SkywarsGame.alive);
        viewers.addAll(SkywarsGame.spectator);
        return viewers;
    }
}

