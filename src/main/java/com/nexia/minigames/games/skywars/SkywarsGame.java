package com.nexia.minigames.games.skywars;

import  com.combatreforged.metis.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.util.BwUtil;
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
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

public class SkywarsGame {
    public static ArrayList<ServerPlayer> alive = new ArrayList<>();

    public static ArrayList<ServerPlayer> spectator = new ArrayList<>();

    public static ServerLevel world = null;

    public static SkywarsMap map = SkywarsMap.RELIC;

    public static RuntimeWorldConfig config = new RuntimeWorldConfig()
            .setDimensionType(FfaAreas.ffaWorld.dimensionType())
            .setGenerator(FfaAreas.ffaWorld.getChunkSource().getGenerator())
            .setDifficulty(Difficulty.EASY)
            .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
            .setGameRule(GameRules.RULE_MOBGRIEFING, false)
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

    public static ArrayList<ServerPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;
    public static boolean isGlowingActive = false;

    private static ServerPlayer winner = null;

    private static int endTime = 5;

    public static CustomBossEvent BOSSBAR = ServerTime.minecraftServer.getCustomBossEvents().get(new ResourceLocation("skywars", "timer"));

    public static void leave(ServerPlayer minecraftPlayer) {
        SkywarsGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);
        SkywarsGame.spectator.remove(minecraftPlayer);
        SkywarsGame.queue.remove(minecraftPlayer);
        SkywarsGame.alive.remove(minecraftPlayer);

        data.kills = 0;

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        player.removeTag("skywars");

        data.gameMode = SkywarsGameMode.LOBBY;
    }

    public static void second() {
        if(SkywarsGame.isStarted) {

            if(SkywarsGame.isEnding) {
                int color = 160 * 65536 + 248;
                // r * 65536 + g * 256 + b;
                DuelGameHandler.winnerRockets(SkywarsGame.winner, SkywarsGame.world, color);

                if(SkywarsGame.endTime <= 0) {
                    for(ServerPlayer player : SkywarsGame.getViewers()){
                        PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                    }

                    SkywarsGame.resetAll();
                }

                SkywarsGame.endTime--;
            } else {
                SkywarsGame.updateInfo();

                if(SkywarsGame.glowingTime <= 0 && !SkywarsGame.isGlowingActive){
                    SkywarsGame.glowPlayers();
                } else if(SkywarsGame.glowingTime > 0 && !SkywarsGame.isGlowingActive){
                    SkywarsGame.glowingTime--;
                }

                if(SkywarsGame.gameEnd > 0 && !SkywarsGame.isEnding) {
                    SkywarsGame.gameEnd--;
                }

                if(SkywarsGame.gameEnd == 60) SkywarsGame.sendCenterWarning();
                if(SkywarsGame.gameEnd <= 0) SkywarsGame.winNearestCenter();
            }


        } else {
            if(SkywarsGame.queue.size() >= 2) {
                for(ServerPlayer player : SkywarsGame.queue){
                    Player fPlayer = PlayerUtil.getFactoryPlayer(player);

                    if(SkywarsGame.queueTime <= 5) {
                        TextColor color = NamedTextColor.GREEN;

                        if(SkywarsGame.queueTime <= 3 && SkywarsGame.queueTime > 1) {
                            color = NamedTextColor.YELLOW;
                        } else if(SkywarsGame.queueTime <= 1) {
                            color = NamedTextColor.RED;
                        }

                        Title title = Title.title(Component.text(SkywarsGame.queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));

                        PlayerUtil.getFactoryPlayer(player).sendTitle(title);
                        PlayerUtil.sendSound(player, new EntityPos(player), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    fPlayer.sendActionBarMessage(
                            Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                                    .append(Component.text(StringUtil.capitalize(SkywarsGame.map.id)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                    .append(Component.text(" (" + SkywarsGame.queue.size() + "/" + SkywarsGame.map.maxPlayers + ")").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(SkywarsGame.queueTime).color(ChatFormat.brandColor2))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Teaming is not allowed!").color(ChatFormat.failColor))
                    );
                }
                if(SkywarsGame.queueTime <= 5 || SkywarsGame.queueTime == 10 || SkywarsGame.queueTime == 15) PlayerUtil.broadcast(SkywarsGame.queue, "§7The game will start in §5" + SkywarsGame.queueTime + " §7seconds.");

                SkywarsGame.queueTime--;
            } else {
                SkywarsGame.queueTime = 15;
            }
            if(SkywarsGame.queueTime <= 0) startGame();
        }
    }

    public static void joinQueue(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);
        data.kills = 0;
        PlayerUtil.resetHealthStatus(player);

        if(SkywarsGame.isStarted || SkywarsGame.queue.size() >= SkywarsGame.map.maxPlayers){
            SkywarsGame.spectator.add(player);
            PlayerDataManager.get(player).gameMode = SkywarsGameMode.SPECTATOR;
            PlayerUtil.sendBossbar(SkywarsGame.BOSSBAR, player, false);
            player.setGameMode(GameType.SPECTATOR);
        } else {
            SkywarsGame.queue.add(player);
            player.setGameMode(GameType.ADVENTURE);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.teleportTo(world, 0, 128.1, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 128, 0), 0, true, false);
    }

    public static void resetMap() {
        SkywarsMap.deleteWorld(SkywarsGame.id);

        SkywarsGame.id = UUID.randomUUID().toString();

        SkywarsGame.map = SkywarsMap.skywarsMaps.get(RandomUtil.randomInt(SkywarsMap.skywarsMaps.size()));
        ServerLevel level = ServerTime.fantasy.openTemporaryWorld(
                SkywarsGame.config,
                new ResourceLocation("skywars", SkywarsGame.id)).asWorld();

        SkywarsGame.map.structureMap.pasteMap(level);
        ServerTime.metisServer.runCommand(String.format("execute in skywars:%s run worldborder set 200", SkywarsGame.id));
        SkywarsMap.spawnQueueBuild(level);
        SkywarsGame.world = level;
    }

    public static void startGame() {
        SkywarsGame.isStarted = true;
        SkywarsGame.isGlowingActive = false;
        SkywarsGame.isEnding = false;
        SkywarsGame.winner = null;
        SkywarsGame.glowingTime = 180;
        SkywarsGame.gameEnd = 360;
        SkywarsGame.alive.addAll(SkywarsGame.queue);

        ArrayList<EntityPos> positions = new ArrayList<>(SkywarsGame.map.positions);

        for (ServerPlayer player : SkywarsGame.alive) {
            EntityPos pos = positions.get(RandomUtil.randomInt(positions.size()));

            PlayerDataManager.get(player).gameMode = SkywarsGameMode.PLAYING;
            player.addTag("skywars");
            player.addTag(LobbyUtil.NO_SATURATION_TAG);
            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.setGameMode(GameType.SURVIVAL);
            pos.teleportPlayer(SkywarsGame.world, player);

            positions.remove(pos);
        }

        SkywarsGame.spectator.clear();
        SkywarsGame.queue.clear();

        PlayerUtil.sendBossbar(SkywarsGame.BOSSBAR, SkywarsGame.getViewers());
    }

    public static void resetAll() {
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

    public static void endGame() {

        SkywarsGame.isEnding = true;

        ServerPlayer serverPlayer = SkywarsGame.alive.stream().findFirst().get();
        SkywarsGame.winner = serverPlayer;

        PlayerDataManager.get(serverPlayer).savedData.wins++;

        for(ServerPlayer player : SkywarsGame.getViewers()){
            PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(Component.text(serverPlayer.getScoreboardName()).color(ChatFormat.brandColor2), Component.text("has won the game!").color(ChatFormat.normalColor)
                    .append(Component.text(" [")
                            .color(ChatFormat.lineColor))
                    .append(Component.text(FfaUtil.calculateHealth(serverPlayer.getHealth()) + "❤").color(ChatFormat.failColor))
                    .append(Component.text("]").color(ChatFormat.lineColor))
            ));
        }
    }

    public static void updateInfo() {
        CustomBossEvent bossbar = SkywarsGame.BOSSBAR;

        for(ServerPlayer player : SkywarsGame.getViewers()) {
            PlayerUtil.getFactoryPlayer(player).sendActionBarMessage(
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
            String parsedTime = BanHandler.banTimeToText(glowingTime * 1000L);
            TextComponent updatedTime = new TextComponent("§7Glow in §a" + parsedTime + "§7...");

            bossbar.setValue(glowingTime);
            bossbar.setName(updatedTime);
            return;
        }

        if(SkywarsGame.gameEnd > 0) {
            String parsedTime = BanHandler.banTimeToText(gameEnd * 1000L);
            TextComponent updatedTime = new TextComponent("§7Game end in §a" + parsedTime + "§7...");

            bossbar.setValue(gameEnd);
            bossbar.setName(updatedTime);
            return;
        }
    }

    public static void glowPlayers() {
        SkywarsGame.isGlowingActive = true;
        SkywarsGame.alive.forEach((player) -> player.setGlowing(true));

        for(ServerPlayer player : SkywarsGame.getViewers()) {
            Player fPlayer = PlayerUtil.getFactoryPlayer(player);
            PlayerUtil.sendSound(player, new EntityPos(player.position()), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.AMBIENT, 1000, 1);
            fPlayer.sendMessage(
                    Component.text("[").color(ChatFormat.lineColor)
                            .append(Component.text("⚠").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            .append(Component.text(" All players have received glowing.").color(TextColor.fromHexString("#FFE588")))
            );

            fPlayer.sendTitle(
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

            fPlayer.sendTitle(
                    Title.title(Component.text("⚠").color(ChatFormat.failColor),
                            Component.text(" In 1 minute, the closest player to center will win.").color(TextColor.fromHexString("#FFE588")))
            );
        }
    }

    public static void winNearestCenter() {
        ServerPlayer closestPlayer = (ServerPlayer) SkywarsGame.world.getNearestPlayer(0, 80, 0, 0, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        for(ServerPlayer death : SkywarsGame.alive) {
            if(death == closestPlayer) return;
            death.die(DamageSource.OUT_OF_WORLD);
        }
    }

    public static boolean isSkywarsPlayer(net.minecraft.world.entity.player.Player player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.SKYWARS || player.getTags().contains("skywars");
    }

    public static void death(ServerPlayer victim, DamageSource source){
        PlayerData victimData = PlayerDataManager.get(victim);
        if(SkywarsGame.isStarted && SkywarsGame.alive.contains(victim) && victimData.gameMode == SkywarsGameMode.PLAYING) {
            ServerPlayer attacker = null;

            if(source != null && PlayerUtil.getPlayerAttacker(source.getEntity()) != null) {
                attacker = PlayerUtil.getPlayerAttacker(source.getEntity());
            }

            if(attacker != null){
                PlayerData attackerData = PlayerDataManager.get(attacker);
                attackerData.kills++;
                attackerData.savedData.kills++;
            }

            victim.destroyVanishingCursedItems();
            victim.inventory.dropAll();

            victimData.savedData.losses++;
            SkywarsGame.alive.remove(victim);
            SkywarsGame.spectator.add(victim);
            PlayerDataManager.get(victim).gameMode = SkywarsGameMode.SPECTATOR;

            String mainColor = LegacyChatFormat.chatColor2;
            String message = mainColor + victim.getCombatTracker().getDeathMessage().getString();

            message = BwUtil.replaceDisplayName(message, mainColor, victim);
            if(attacker != null) message = BwUtil.replaceDisplayName(message, mainColor, attacker);

            PlayerUtil.broadcast(SkywarsGame.getViewers(), message);

            if(SkywarsGame.alive.size() == 1) {
                SkywarsGame.endGame();
            }
        }
    }

    public static void firstTick(){
        SkywarsGame.resetAll();
    }

    public static ArrayList<ServerPlayer> getViewers() {
        ArrayList<ServerPlayer> viewers = new ArrayList<>(SkywarsGame.alive);
        viewers.addAll(SkywarsGame.spectator);
        return viewers;
    }
}

