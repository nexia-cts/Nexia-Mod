package com.nexia.minigames.games.skywars;

import  com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.skywars.util.player.PlayerData;
import com.nexia.minigames.games.skywars.util.player.PlayerDataManager;
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
    public static int glowingTime = 300;
    public static int gameEnd = 600;
    public static int queueTime = 15;

    private static int boxTime = 5;

    public static ArrayList<ServerPlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;
    public static boolean isBoxing = false;
    public static boolean isGlowingActive = false;

    private static ServerPlayer winner = null;

    private static int endTime = 5;


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
                if(SkywarsGame.glowingTime <= 0){
                    SkywarsGame.glowPlayers();
                } else {
                    SkywarsGame.glowingTime--;
                }

                if(SkywarsGame.isBoxing) SkywarsGame.doBoxing();
                if(SkywarsGame.gameEnd == 60) SkywarsGame.sendCenterWarning();
                if(SkywarsGame.gameEnd >= 0) SkywarsGame.winNearestCenter();
            }


        } else {
            if(SkywarsGame.queue.size() >= 2) {
                for(ServerPlayer player : SkywarsGame.queue){
                    PlayerUtil.sendActionbar(player, String.format("§7Map » §5§l%s §7(%s) §8| §7Time » §5§l%s ", SkywarsGame.map.id, SkywarsGame.map.maxPlayers, queueTime));
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
        player.setHealth(player.getMaxHealth());

        if(SkywarsGame.isStarted || SkywarsGame.queue.size() >= SkywarsGame.map.maxPlayers){
            SkywarsGame.spectator.add(player);
            PlayerDataManager.get(player).gameMode = SkywarsGameMode.SPECTATOR;
            player.setGameMode(GameType.SPECTATOR);
        } else {
            SkywarsGame.queue.add(player);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.teleportTo(world, 0, 128.1, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 128, 0), 0, true, false);
    }

    public static void startGame() {
        SkywarsGame.isStarted = true;
        SkywarsGame.isGlowingActive = false;
        SkywarsGame.isEnding = false;
        SkywarsGame.isBoxing = true;
        SkywarsGame.winner = null;
        SkywarsGame.boxTime = 5;
        SkywarsGame.glowingTime = 300;
        SkywarsGame.gameEnd = 600;
        SkywarsGame.alive.addAll(SkywarsGame.queue);


        for(EntityPos pos : SkywarsGame.map.positions) {
            SkywarsMap.createGlassBox(SkywarsGame.world, pos);
        }

        ArrayList<ServerPlayer> teleportedList = SkywarsGame.alive;

        for (EntityPos pos : SkywarsGame.map.positions) {
            if (teleportedList.isEmpty()) return;
            ServerPlayer player = teleportedList.get(RandomUtil.randomInt(teleportedList.size()));

            teleportedList.remove(player);

            PlayerDataManager.get(player).gameMode = SkywarsGameMode.PLAYING;
            player.addTag("skywars");
            player.addTag(LobbyUtil.NO_SATURATION_TAG);
            pos.teleportPlayer(SkywarsGame.world, player);
        }

        SkywarsGame.spectator.clear();
        SkywarsGame.queue.clear();

        PlayerUtil.sendBossbar(ServerTime.minecraftServer.getCustomBossEvents().get(new ResourceLocation("skywars", "timer")), SkywarsGame.getViewers());
    }

    public static void resetAll() {
        SkywarsGame.isStarted = false;
        SkywarsGame.isGlowingActive = false;
        SkywarsGame.glowingTime = 300;
        SkywarsGame.isBoxing = false;
        SkywarsGame.isEnding = false;
        SkywarsGame.boxTime = 5;
        SkywarsGame.gameEnd = 600;
        SkywarsGame.queueTime = 15;

        SkywarsGame.alive.clear();
        SkywarsGame.spectator.clear();
        SkywarsGame.queue.clear();

        SkywarsGame.map = SkywarsMap.skywarsMaps.get(RandomUtil.randomInt(SkywarsMap.skywarsMaps.size()));
        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("skywars", SkywarsGame.map.id), SkywarsGame.config).asWorld();
    }

    public static void endGame() {

        SkywarsGame.isEnding = true;

        ServerPlayer serverPlayer = SkywarsGame.alive.stream().findFirst().get();

        SkywarsGame.winner = serverPlayer;

        Component win = Component.text(serverPlayer.getScoreboardName()).color(ChatFormat.brandColor2)
                .append(Component.text(" has won the game!").color(ChatFormat.normalColor)
                        .append(Component.text(" [")
                                .color(ChatFormat.lineColor))
                        .append(Component.text(FfaUtil.calculateHealth(serverPlayer.getHealth()) + "❤").color(ChatFormat.failColor))
                        .append(Component.text("]").color(ChatFormat.lineColor))
                );

        for(ServerPlayer player : SkywarsGame.getViewers()){
            PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(win, Component.text("")));
        }
    }

    public static void doBoxing() {

        for(ServerPlayer player : SkywarsGame.alive) {
            Player factoryPlayer = PlayerUtil.getFactoryPlayer(player);

            Title title;
            TextColor color = NamedTextColor.GREEN;

            if(SkywarsGame.boxTime <= 3 && SkywarsGame.boxTime > 1) {
                color = NamedTextColor.YELLOW;
            } else if(SkywarsGame.boxTime <= 1) {
                color = NamedTextColor.RED;
            }

            title = Title.title(Component.text(SkywarsGame.boxTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
            factoryPlayer.sendTitle(title);
        }

        if(SkywarsGame.boxTime <= 0) {
            for(EntityPos pos : SkywarsGame.map.positions) {
                SkywarsMap.removeGlassBox(SkywarsGame.world, pos);
            }
            SkywarsGame.isBoxing = false;
        }

        SkywarsGame.boxTime--;
    }

    public static void updateInfo() {
        CustomBossEvent bossbar = ServerTime.minecraftServer.getCustomBossEvents().get(new ResourceLocation("skywars", "timer"));

        for(ServerPlayer player : SkywarsGame.getViewers()) {
            PlayerUtil.sendActionbar(player, String.format("§7Map » §5§l%s §7(%s) §8| §7Players » §5§l%s ", SkywarsGame.map.id, SkywarsGame.map.maxPlayers, SkywarsGame.alive.size()));
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

        ServerTime.factoryServer.runCommand("/title @a[tag=skywars] title {\"text\":\"\\u26a0\",\"color\":\"red\"}", 0, false);
        ServerTime.factoryServer.runCommand("/title @a[tag=skywars] subtitle {\"text\":\"All players have received glowing.\",\"color\":\"#FFE588\"}", 0, false);

        for(ServerPlayer player : SkywarsGame.getViewers()) {
            PlayerUtil.sendSound(player, new EntityPos(player.position()), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.AMBIENT, 1000, 1);
            PlayerUtil.getFactoryPlayer(player).sendMessage(
                    Component.text("[").color(ChatFormat.lineColor)
                            .append(Component.text("⚠").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            .append(Component.text(" All players have received glowing.").color(TextColor.fromHexString("#FFE588")))
            );
        }
    }

    public static void sendCenterWarning() {
        ServerTime.factoryServer.runCommand("/title @a[tag=skywars] title {\"text\":\"\\u26a0\",\"color\":\"red\"}", 0, false);
        ServerTime.factoryServer.runCommand("/title @a[tag=skywars] subtitle {\"text\":\"In 1 minute, the closest player to center will win.\",\"color\":\"#FFE588\"}", 0, false);

        for(ServerPlayer player : SkywarsGame.getViewers()) {
            PlayerUtil.sendSound(player, new EntityPos(player.position()), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.AMBIENT, 1000, 1);
            PlayerUtil.getFactoryPlayer(player).sendMessage(
                    Component.text("[").color(ChatFormat.lineColor)
                            .append(Component.text("⚠").color(ChatFormat.failColor))
                            .append(Component.text("]").color(ChatFormat.lineColor))
                            .append(Component.text(" In 1 minute, the closest player to center will win.").color(TextColor.fromHexString("#FFE588")))
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

    public static boolean isSkywarsPlayer(ServerPlayer player){
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

            SkywarsGame.alive.remove(victim);
            SkywarsGame.joinQueue(victim);

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

