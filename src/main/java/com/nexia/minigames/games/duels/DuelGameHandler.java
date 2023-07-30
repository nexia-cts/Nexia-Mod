package com.nexia.minigames.games.duels;

import com.nexia.core.Main;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelGameHandler {

    public static List<DuelsGame> duelsGames = new ArrayList<>();
    public static List<TeamDuelsGame> teamDuelsGames = new ArrayList<>();

    public static void leave(ServerPlayer player, boolean leaveTeam) {
        PlayerData data = PlayerDataManager.get(player);
        if (data.duelsGame != null) {
            data.duelsGame.death(player, player.getLastDamageSource());
        }
        if (data.teamDuelsGame != null) {
            data.teamDuelsGame.death(player, player.getLastDamageSource());
        }
        if (data.gameMode == DuelGameMode.SPECTATING) {
            GamemodeHandler.unspectatePlayer(player, data.spectatingPlayer, false);
        }
        data.inviting = false;
        data.inDuel = false;
        data.duelPlayer = null;
        removeQueue(player, null, true);
        data.gameMode = DuelGameMode.LOBBY;
        data.spectatingPlayer = null;
        if (leaveTeam) {
            if (data.duelsTeam != null) {
                if (data.duelsTeam.refreshLeader(player)) {
                    data.duelsTeam.disbandTeam(player, true);
                } else {
                    data.duelsTeam.leaveTeam(player, true);
                }
            }
            data.duelsTeam = null;
        }
        data.teamDuelsGame = null;
        data.duelsGame = null;
    }

    public static void winnerRockets(@NotNull ServerPlayer winner, @NotNull ServerLevel level,
                                     @NotNull Integer winnerColor) {

        Random random = level.getRandom();
        EntityPos pos = new EntityPos(winner).add(random.nextInt(9) - 4, 2, random.nextInt(9) - 4);

        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
        try {
            itemStack.setTag(TagParser.parseTag("{Fireworks:{Explosions:[{Type:0,Flicker:1b,Trail:1b,Colors:[I;" +
                    winnerColor + "]}]}}"));
        } catch (Exception ignored) {
        }

        FireworkRocketEntity rocket = new FireworkRocketEntity(level, pos.x, pos.y, pos.z, itemStack);
        level.addFreshEntity(rocket);
    }

    public static void starting() {
        for (DuelGameMode duelGameMode : DuelGameMode.duelGameModes) {
            duelGameMode.queue.clear();
        }

        DuelGameHandler.duelsGames.clear();
        DuelGameHandler.teamDuelsGames.clear();

        List<String> toDelete = new ArrayList<>();

        for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            String[] split = level.dimension().toString().replaceAll("]", "").split(":");
            if (split[1].toLowerCase().contains("duels")) {
                toDelete.add(split[2]);
            }
        }

        for (String deletion : toDelete) {
            deleteWorld(deletion);
        }
    }

    /*
    public static String returnCommandMap(DuelsMap map) {
        if (map.structureMap.rotation != Rotation.NO_ROTATION) {
            return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + map.id + "'" + ",posX:"
                    + map.structureMap.pastePos.getX() + ",posY:" + map.structureMap.pastePos.getY() + ",posZ:" + map.structureMap.pastePos.getZ() + ",rotation:\"" + map.structureMap.rotation.id + "\"}";
        } else {
            return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + map.id + "'" + ",posX:"
                    + map.structureMap.pastePos.getX() + ",posY:" + map.structureMap.pastePos.getY() + ",posZ:" + map.structureMap.pastePos.getZ() + "}";
        }

    }
     */


    public static ServerLevel createWorld(String uuid, boolean doRegeneration) {
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(FfaAreas.ffaWorld.dimensionType())
                .setGenerator(FfaAreas.ffaWorld.getChunkSource().getGenerator())
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                .setGameRule(GameRules.RULE_DAYLIGHT, false)
                .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                .setGameRule(GameRules.RULE_NATURAL_REGENERATION, doRegeneration)
                .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0)
                .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
                .setTimeOfDay(6000);


        return ServerTime.fantasy.openTemporaryWorld(config, new ResourceLocation("duels", uuid)).asWorld();
        //return ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", uuid)).location(), config).asWorld();
    }

    public static void deleteWorld(String id) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", id)).location(),
                    new RuntimeWorldConfig());
            ServerTime.factoryServer.unloadWorld("duels:" + id, false);
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/duels", id));
        } catch (Exception ignored) {
            Main.logger.error("Error occurred while deleting world: duels:" + id);
            return;
        }
        worldHandle.delete();
    }
}