package com.nexia.minigames.games.duels.custom.kitroom.kitrooms;

import com.nexia.core.Main;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.DimensionType;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.world.structure.Rotation;
import net.notcoded.codelib.util.world.structure.StructureMap;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.UUID;

import static com.nexia.world.WorldUtil.getChunkGenerator;

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

    public void setType(String string) {
        if(string.equalsIgnoreCase("custom") || string.equalsIgnoreCase("vanilla") || string.equalsIgnoreCase("smp")) {
            this.kitRoom = new StructureMap(
                    new ResourceLocation("duels", "kitroom_" + string.toLowerCase()),
                    Rotation.NO_ROTATION,
                    true,
                    new BlockPos(0, 80, 0),
                    new BlockPos(0, 80, 0),
                    true
            );
        }
    }

    public boolean generate() {
        System.out.println("yes1");
        if(this.kitRoom == null) return false;
        this.uuid = UUID.randomUUID();
        System.out.println(this.uuid);

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(DimensionType.OVERWORLD_LOCATION)
                .setGenerator(getChunkGenerator())
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

        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Created world: kitroom:" + uuid);

        this.handle = ServerTime.fantasy.openTemporaryWorld(config, new ResourceLocation("kitroom", String.valueOf(uuid)));
        this.level = this.handle.asWorld();
        this.hasBeenGenerated = this.level != null;

        System.out.println(this.hasBeenGenerated);

        this.kitRoom.pasteMap(this.level);

        return this.hasBeenGenerated;
    }

    public boolean teleport() {
        System.out.println("teleporttest");
        if(!this.hasBeenGenerated || this.level == null) return false;

        this.player.get().teleportTo(this.level, 0, 80, 0, 0, 0);
        this.player.get().clearFire();
        this.player.get().getEnderChestInventory().clearContent();
        this.player.get().inventory.clearContent();
        this.player.get().setExperiencePoints(0);
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
        this.player.get().getEnderChestInventory().clearContent();
        this.player.get().setExperiencePoints(0);
        this.player.get().setExperienceLevels(0);
        PlayerUtil.resetHealthStatus(this.player.get());
        this.delete();
    }


    private void delete() {
        if(this.level == null || this.handle == null) return;
        this.handle.delete();
    }
}
