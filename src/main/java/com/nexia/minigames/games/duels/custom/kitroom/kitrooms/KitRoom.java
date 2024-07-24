package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.api.world.util.Location;
import com.nexia.nexus.builder.implementation.world.structure.StructureMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
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

    public NexiaPlayer player;

    private StructureMap kitRoom;

    public KitRoom(NexiaPlayer player) {
        this.player = player;

        this.uuid = null;
        this.level = null;
        this.kitRoom = null;
        this.handle = null;
        this.hasBeenGenerated = false;
    }

    public static boolean isInKitRoom(NexiaPlayer player) {
        return ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).kitRoom != null && !((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player)).editingKit.isEmpty();
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
                .setGenerator(getChunkGenerator(Biomes.THE_VOID))
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

        if(NexiaCore.config.debugMode) NexiaCore.logger.info("[DEBUG]: Created world: kitroom:{}", uuid);

        this.handle = ServerTime.fantasy.openTemporaryWorld(config, new ResourceLocation("kitroom", String.valueOf(uuid)));
        this.level = this.handle.asWorld();
        this.hasBeenGenerated = this.level != null;

        this.getKitRoom().pasteMap(this.level);

        return this.hasBeenGenerated;
    }

    public boolean teleport() {
        if(!this.hasBeenGenerated || this.level == null) return false;

        this.player.teleport(new Location(0.5, 80, 0.5, 0, 0, WorldUtil.getWorld(this.level)));
        this.player.reset(true, Minecraft.GameMode.ADVENTURE);
        this.player.addTag("in_kitroom");

        return true;
    }

    public void leave() {
        ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(this.player)).kitRoom = null;
        this.player.reset(true, Minecraft.GameMode.ADVENTURE);
        this.delete();
    }


    private void delete() {
        if(this.level == null || this.handle == null) return;
        WorldUtil.deleteWorld(new ResourceLocation("kitroom", String.valueOf(uuid)));
    }
}
