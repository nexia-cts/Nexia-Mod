package com.nexia.minigames.games.duels;

import com.nexia.core.Main;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.duels.team.TeamDuelsGame;
import com.nexia.minigames.games.duels.util.player.PlayerData;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.nexia.minigames.games.duels.gamemodes.GamemodeHandler.removeQueue;

public class DuelGameHandler {

    public static List<DuelsGame> duelsGames = new ArrayList<>();
    public static List<TeamDuelsGame> teamDuelsGames = new ArrayList<>();
    public static void leave(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data.duelsGame != null) {
            data.duelsGame.death(player, player.getLastDamageSource());
        }
        if(data.teamDuelsGame != null) {
            data.teamDuelsGame.death(player, player.getLastDamageSource());
        }
        if(data.gameMode == DuelGameMode.SPECTATING) {
            GamemodeHandler.unspectatePlayer(player, data.spectatingPlayer, false);
        }
        data.inviting = false;
        data.inDuel = false;
        data.duelPlayer = null;
        removeQueue(player, null, true);
        data.gameMode = DuelGameMode.LOBBY;
        data.spectatingPlayer = null;
        data.duelsTeam = null;
        data.teamDuelsGame = null;
        data.duelsGame = null;
    }

    public static void winnerRockets(@NotNull ServerPlayer winner, @NotNull ServerLevel level, @NotNull Integer winnerColor) {

        Random random = level.getRandom();
        EntityPos pos = new EntityPos(winner).add(random.nextInt(9) - 4, 2, random.nextInt(9) - 4);

        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
        try {
            itemStack.setTag(TagParser.parseTag("{Fireworks:{Explosions:[{Type:0,Flicker:1b,Trail:1b,Colors:[I;" +
                    winnerColor + "]}]}}"));
        } catch (Exception ignored) {}

        FireworkRocketEntity rocket = new FireworkRocketEntity(level, pos.x, pos.y, pos.z, itemStack);
        level.addFreshEntity(rocket);
    }

    public static void starting() {
        DuelGameMode.AXE_QUEUE.clear();
        DuelGameMode.SWORD_ONLY_QUEUE.clear();
        DuelGameMode.FFA_QUEUE.clear();
        DuelGameMode.TRIDENT_ONLY_QUEUE.clear();
        DuelGameMode.HOE_ONLY_QUEUE.clear();
        DuelGameMode.UHC_QUEUE.clear();
        DuelGameMode.BOW_ONLY_QUEUE.clear();
        DuelGameMode.VANILLA_QUEUE.clear();
        DuelGameMode.SHIELD_QUEUE.clear();
        DuelGameMode.POT_QUEUE.clear();
        DuelGameMode.NETH_POT_QUEUE.clear();
        DuelGameMode.OG_VANILLA_QUEUE.clear();
        DuelGameMode.SMP_QUEUE.clear();
        DuelGameMode.UHC_SHIELD_QUEUE.clear();
        DuelGameMode.HSG_QUEUE.clear();
        DuelGameMode.SKYWARS_QUEUE.clear();
        DuelGameMode.CLASSIC_CRYSTAL_QUEUE.clear();

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

    public static float[] returnPosMap(String mapname, boolean player1){
        float[] pos = new float[]{0, 83, 0, 0, 0};

        if(player1){
            if (mapname.equalsIgnoreCase("city")) {
                pos[0] = -55;
                pos[1] = 81;
                pos[3] = -4;
            } else if (mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
                pos[0] = 0;
                pos[1] = 80;
                pos[2] = -41;
            } else if (mapname.equalsIgnoreCase("plains")) {
                pos[0] = -71;
                pos[1] = 81;
                pos[2] = -16;
            }
        } else {
            if (mapname.equalsIgnoreCase("city")) {
                pos[0] = 17;
                pos[1] = 80;
                pos[2] = -4;
                pos[3] = -90;
            } else if (mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
                pos[0] = 0;
                pos[1] = 80;
                pos[2] = 41;
            } else if (mapname.equalsIgnoreCase("plains")) {
                pos[0] = -71;
                pos[1] = 81;
                pos[2] = 34;
                pos[3] = 180;
            }
        }

        return pos;
    }

    public static String returnCommandMap(String mapname) {

        int[] pos = new int[]{0, 0, 0};

        String rotation = "";
        if (mapname.equalsIgnoreCase("city")) {
            pos[0] = -65;
            pos[1] = -11;
            pos[2] = -31;
        } else if (mapname.equalsIgnoreCase("nethflat") || mapname.equalsIgnoreCase(("netheriteflat"))) {
            pos[0] = -36;
            pos[1] = -3;
            pos[2] = -51;
        } else if (mapname.equalsIgnoreCase("plains")) {
            pos[0] = -40;
            pos[1] = -20;
            pos[2] = -31;
            rotation = "CLOCKWISE_90";
        }

        if(rotation.trim().length() != 0){
            return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + mapname.toLowerCase() + "'" + ",posX:" + pos[0] + ",posY:" + pos[1] + ",posZ:" + pos[2] + ",rotation:\"" + rotation + "\"}";
        } else {
            return "setblock 0 80 0 minecraft:structure_block{mode:'LOAD',name:'duels:" + mapname.toLowerCase() + "'" + ",posX:" + pos[0] + ",posY:" + pos[1] + ",posZ:" + pos[2] + "}";
        }
    }

    public static ServerLevel createWorld(boolean doRegeneration){
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

        //return ServerTime.fantasy.openTemporaryWorld(config).asWorld();


        return ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", UUID.randomUUID().toString().replaceAll("-", ""))).location(), config).asWorld();
    }

    public static void deleteWorld(String id) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("duels", id)).location(), null);
        } catch (Exception ignored) {
            Main.logger.error("Error occurred while deleting world: duels:" + id);
            return;
        }
        worldHandle.delete();
    }
}