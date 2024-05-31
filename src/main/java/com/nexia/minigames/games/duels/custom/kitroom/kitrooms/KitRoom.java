package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.Main;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.world.StructureMap;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.notcoded.codelib.players.AccuratePlayer;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.UUID;

import static com.nexia.core.utilities.world.WorldUtil.getChunkGenerator;

public class KitRoom {

    // Kit room generator

    public boolean hasBeenGenerated;

    public UUID uuid;

    public ServerLevel level;

    private RuntimeWorldHandle handle;

    // Other

    public AccuratePlayer player;

    private StructureMap kitRoom;

    public KitRoom(AccuratePlayer player) {
        this.player = player;

        this.uuid = null;
        this.level = null;
        this.kitRoom = null;
        this.handle = null;
        this.hasBeenGenerated = false;
    }

    public static boolean isInKitRoom(Player player) {
        return PlayerDataManager.get(player).kitRoom != null && !PlayerDataManager.get(player).editingKit.isEmpty();
    }

    public StructureMap getKitRoom() {
        return this.kitRoom;
    }

    public StructureMap setKitRoom(StructureMap structureMap) {
        this.kitRoom = structureMap;
        return this.kitRoom;
    }

    public boolean generate() {
        if(this.getKitRoom() == null) return false;
        this.uuid = UUID.randomUUID();

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(DimensionType.OVERWORLD_LOCATION)
                .setGenerator(getChunkGenerator(Biomes.PLAINS))
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.RULE_KEEPINVENTORY, false)
                .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                .setGameRule(GameRules.RULE_DAYLIGHT, false)
                .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, false)
                .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                .setGameRule(GameRules.RULE_NATURAL_REGENERATION, true)
                .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, false)
                .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0)
                .setGameRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS, false)
                .setTimeOfDay(6000);

        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Created world: kitroom:{}", uuid);

        this.handle = ServerTime.fantasy.openTemporaryWorld(config, new ResourceLocation("kitroom", String.valueOf(uuid)));
        this.level = this.handle.asWorld();
        this.hasBeenGenerated = this.level != null;

        this.getKitRoom().pasteMap(this.level);

        return this.hasBeenGenerated;
    }

    public boolean teleport() {
        if(!this.hasBeenGenerated || this.level == null) return false;

        this.player.get().teleportTo(this.level, 0.5, 80, 0.5, 0, 0);
        this.player.get().clearFire();
        this.player.get().getEnderChestInventory().clearContent();
        this.player.get().inventory.clearContent();
        this.player.get().setExperiencePoints(0);
        this.player.get().setGameMode(GameType.ADVENTURE);
        this.player.get().setExperienceLevels(0);
        PlayerUtil.resetHealthStatus(this.player.get());

        this.player.get().addTag("in_kitroom");

        return true;
    }

    public void leave() {
        PlayerDataManager.get(this.player.get()).kitRoom = null;
        this.player.get().inventory.clearContent();
        this.player.get().getEnderChestInventory().clearContent();
        this.player.get().clearFire();
        this.player.get().setExperiencePoints(0);
        this.player.get().setExperienceLevels(0);
        PlayerUtil.resetHealthStatus(this.player.get());
        this.delete();
    }


    private void delete() {
        if(this.level == null || this.handle == null) return;
        WorldUtil.deleteWorld(new Identifier("kitroom", String.valueOf(uuid)));
    }
}
