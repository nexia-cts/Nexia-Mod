package com.nexia.minigames.games.bedwars.players;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.minigames.games.bedwars.areas.BedwarsAreas;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BedwarsGame;
import com.nexia.minigames.games.bedwars.util.BedwarsScoreboard;
import net.kyori.adventure.text.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

public class BedwarsPlayers {

    public static final String BED_WARS_IN_GAME_TAG = "in_bedwars";

    private static void setInBedWars(NexiaPlayer player) {
        CorePlayerData playerData = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);

        playerData.gameMode = PlayerGameMode.BEDWARS;
        player.addTag(BedwarsPlayers.BED_WARS_IN_GAME_TAG);
    }

    public static void joinQueue(NexiaPlayer player) {
        setInBedWars(player);

        BedwarsAreas.queueSpawn.teleportPlayer(BedwarsAreas.bedWarsWorld, player.unwrap());
        player.unwrap().setRespawnPosition(BedwarsAreas.bedWarsWorld.dimension(),
                BedwarsAreas.queueSpawn.toBlockPos(), BedwarsAreas.queueSpawn.yaw, true, false);

        player.reset(true, Minecraft.GameMode.ADVENTURE);

        //player.setInvulnerable(true);
        player.unwrap().addTag(LobbyUtil.NO_DAMAGE_TAG);

        BedwarsGame.queueList.add(player);
        if (!BedwarsGame.isQueueCountdownActive && BedwarsGame.queueList.size() >= BedwarsGame.requiredPlayers) {
            BedwarsGame.startQueueCountdown();
        }
    }

    public static void leaveQueue(NexiaPlayer player) {
        BedwarsGame.queueList.remove(player);
        if (BedwarsGame.isQueueCountdownActive && BedwarsGame.queueList.size() < BedwarsGame.requiredPlayers) {
            BedwarsGame.endQueueCountdown();
        }
    }

    public static void eliminatePlayer(NexiaPlayer player, boolean becomeSpectator) {
        BedwarsTeam team = BedwarsTeam.getPlayerTeam(player);

        if (team != null) {
            Component eliminationMessage = Component.text(team.textColor + player.getRawName()).append(Component.text(" has been eliminated", ChatFormat.systemColor));

            for(NexiaPlayer nexiaPlayers : getPlayers()) {
                nexiaPlayers.sendMessage(eliminationMessage);
            }

            PlayerDataManager.getDataManager(NexiaCore.BEDWARS_DATA_MANAGER).get(player).savedData.incrementInteger("losses");
            team.players.remove(player);

            ServerLevel world = BedwarsAreas.bedWarsWorld;
            if (team.players.isEmpty() && team.bedLocation != null &&
                    world.getBlockState(team.bedLocation).getBlock() instanceof BedBlock) {
                world.setBlock(team.bedLocation, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        player.unwrap().getEnderChestInventory().clearContent();
        BedwarsScoreboard.updateScoreboard();

        if (becomeSpectator) {
            becomeSpectator(player);
        } else {
            BedwarsScoreboard.removeScoreboardFor(player);
        }

        if (BedwarsTeam.getAliveTeams().size() < 2) {
            BedwarsGame.endBedwars();
        }
    }

    public static void becomeSpectator(NexiaPlayer player) {
        setInBedWars(player);

        player.setGameMode(Minecraft.GameMode.SPECTATOR);
        BedwarsAreas.spectatorSpawn.teleportPlayer(BedwarsAreas.bedWarsWorld, player.unwrap());
        player.unwrap().setRespawnPosition(BedwarsAreas.bedWarsWorld.dimension(),
                BedwarsAreas.spectatorSpawn.toBlockPos(), BedwarsAreas.spectatorSpawn.yaw, true, false);

        BedwarsGame.spectatorList.add(player);
        player.addTag(LobbyUtil.NO_RANK_DISPLAY_TAG);
        ServerScoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
        scoreboard.addPlayerToTeam(player.getRawName(), BedwarsGame.spectatorTeam);

        BedwarsScoreboard.sendBedWarsScoreboard(player);
        BedwarsScoreboard.sendLines(player);
    }

    public static void sendToSpawn(NexiaPlayer player) {
        player.safeReset(true, Minecraft.GameMode.SURVIVAL);
        giveSpawnItems(player);
        player.unwrap().setInvulnerable(true);
        BedwarsGame.invulnerabilityList.put(player, BedwarsGame.invulnerabilityTime * 20);

        EntityPos respawnPos = BedwarsAreas.bedWarsCenter;
        BedwarsTeam team = BedwarsTeam.getPlayerTeam(player);
        if (team != null && team.spawn != null) {
            respawnPos = team.spawn;
        }
        player.unwrap().setRespawnPosition(BedwarsAreas.bedWarsWorld.dimension(), respawnPos.toBlockPos(), respawnPos.yaw, true, false);
        player.unwrap().teleportTo(BedwarsAreas.bedWarsWorld, respawnPos.x, respawnPos.y, respawnPos.z, respawnPos.yaw, respawnPos.pitch);
    }

    private static void giveSpawnItems(NexiaPlayer player) {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.getOrCreateTag().putInt("Unbreakable", 1);
        player.unwrap().inventory.add(sword);

        if (player.unwrap().inventory.getItem(36).isEmpty()) {
            player.unwrap().inventory.setItem(36, getArmorItem(Items.LEATHER_BOOTS, player));
        }
        if (player.unwrap().inventory.getItem(37).isEmpty()) {
            player.unwrap().inventory.setItem(37, getArmorItem(Items.LEATHER_LEGGINGS, player));
        }
        player.unwrap().inventory.setItem(38, getArmorItem(Items.LEATHER_CHESTPLATE, player));
        player.unwrap().inventory.setItem(39, getArmorItem(Items.LEATHER_HELMET, player));
    }

    private static ItemStack getArmorItem(Item item, NexiaPlayer player) {
        ItemStack itemStack = item.getDefaultInstance();
        itemStack.getOrCreateTag().putInt("Unbreakable", 1);

        if (!(item instanceof DyeableLeatherItem leatherItem)) return itemStack;

        BedwarsTeam team = BedwarsTeam.getPlayerTeam(player);
        if (team == null) return itemStack;

        leatherItem.setColor(itemStack, team.armorColor);
        return itemStack;
    }

    // Lists ----------------------------------------------------
    // A viewer is someone playing the game or spectating the game

    public static ArrayList<NexiaPlayer> getViewers() {
        ArrayList<NexiaPlayer> viewers = new ArrayList<>(getPlayers());
        viewers.addAll(BedwarsGame.spectatorList);
        return viewers;
    }

    public static ArrayList<NexiaPlayer> getPlayers() {
        ArrayList<NexiaPlayer> list = new ArrayList<>();
        for (BedwarsTeam team : BedwarsTeam.allTeams.values()) {
            if (team.players != null) {
                list.addAll(team.players);
            }
        }
        return list;
    }

}
