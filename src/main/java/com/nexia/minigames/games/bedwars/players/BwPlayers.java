package com.nexia.minigames.games.bedwars.players;

import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.BwGame;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
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

public class BwPlayers {

    public static final String BED_WARS_IN_GAME_TAG = "in_bedwars";

    private static void setInBedWars(NexiaPlayer player) {
        PlayerData playerData = PlayerDataManager.get(player);

        playerData.gameMode = PlayerGameMode.BEDWARS;
        player.addTag(BwPlayers.BED_WARS_IN_GAME_TAG);
    }

    public static void joinQueue(NexiaPlayer player) {
        setInBedWars(player);

        BwAreas.queueSpawn.teleportPlayer(BwAreas.bedWarsWorld, player.unwrap());
        player.unwrap().setRespawnPosition(BwAreas.bedWarsWorld.dimension(),
                BwAreas.queueSpawn.toBlockPos(), BwAreas.queueSpawn.yaw, true, false);

        player.reset(true, Minecraft.GameMode.ADVENTURE);

        //player.setInvulnerable(true);
        player.unwrap().addTag(LobbyUtil.NO_DAMAGE_TAG);

        BwGame.queueList.add(player);
        if (!BwGame.isQueueCountdownActive && BwGame.queueList.size() >= BwGame.requiredPlayers) {
            BwGame.startQueueCountdown();
        }
    }

    public static void leaveQueue(NexiaPlayer player) {
        BwGame.queueList.remove(player);
        if (BwGame.isQueueCountdownActive && BwGame.queueList.size() < BwGame.requiredPlayers) {
            BwGame.endQueueCountdown();
        }
    }

    public static void eliminatePlayer(NexiaPlayer player, boolean becomeSpectator) {
        BwTeam team = BwTeam.getPlayerTeam(player);

        if (team != null) {
            Component eliminationMessage = Component.text(team.textColor + player.getRawName()).append(Component.text(" has been eliminated", ChatFormat.systemColor));

            for(NexiaPlayer nexiaPlayers : getPlayers()) {
                nexiaPlayers.sendMessage(eliminationMessage);
            }

            com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.get(player).savedData.loss++;
            team.players.remove(player);

            ServerLevel world = BwAreas.bedWarsWorld;
            if (team.players.isEmpty() && team.bedLocation != null &&
                    world.getBlockState(team.bedLocation).getBlock() instanceof BedBlock) {
                world.setBlock(team.bedLocation, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        player.unwrap().getEnderChestInventory().clearContent();
        BwScoreboard.updateScoreboard();

        if (becomeSpectator) {
            becomeSpectator(player);
        } else {
            BwScoreboard.removeScoreboardFor(player);
        }

        if (BwTeam.getAliveTeams().size() < 2) {
            BwGame.endBedwars();
        }
    }

    public static void becomeSpectator(NexiaPlayer player) {
        setInBedWars(player);

        player.setGameMode(Minecraft.GameMode.SPECTATOR);
        BwAreas.spectatorSpawn.teleportPlayer(BwAreas.bedWarsWorld, player.unwrap());
        player.unwrap().setRespawnPosition(BwAreas.bedWarsWorld.dimension(),
                BwAreas.spectatorSpawn.toBlockPos(), BwAreas.spectatorSpawn.yaw, true, false);

        BwGame.spectatorList.add(player);
        player.addTag(LobbyUtil.NO_RANK_DISPLAY_TAG);
        ServerScoreboard scoreboard = ServerTime.minecraftServer.getScoreboard();
        scoreboard.addPlayerToTeam(player.getRawName(), BwGame.spectatorTeam);

        BwScoreboard.sendBedWarsScoreboard(player);
        BwScoreboard.sendLines(player);
    }

    public static void sendToSpawn(NexiaPlayer player) {
        player.safeReset(true, Minecraft.GameMode.SURVIVAL);
        giveSpawnItems(player);
        player.unwrap().setInvulnerable(true);
        BwGame.invulnerabilityList.put(player, BwGame.invulnerabilityTime * 20);

        EntityPos respawnPos = BwAreas.bedWarsCenter;
        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team != null && team.spawn != null) {
            respawnPos = team.spawn;
        }
        player.unwrap().setRespawnPosition(BwAreas.bedWarsWorld.dimension(), respawnPos.toBlockPos(), respawnPos.yaw, true, false);
        player.unwrap().teleportTo(BwAreas.bedWarsWorld, respawnPos.x, respawnPos.y, respawnPos.z, respawnPos.yaw, respawnPos.pitch);
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

        BwTeam team = BwTeam.getPlayerTeam(player);
        if (team == null) return itemStack;

        leatherItem.setColor(itemStack, team.armorColor);
        return itemStack;
    }

    // Lists ----------------------------------------------------
    // A viewer is someone playing the game or spectating the game

    public static ArrayList<NexiaPlayer> getViewers() {
        ArrayList<NexiaPlayer> viewers = new ArrayList<>(getPlayers());
        viewers.addAll(BwGame.spectatorList);
        return viewers;
    }

    public static ArrayList<NexiaPlayer> getPlayers() {
        ArrayList<NexiaPlayer> list = new ArrayList<>();
        for (BwTeam team : BwTeam.allTeams.values()) {
            if (team.players != null) {
                list.addAll(team.players);
            }
        }
        return list;
    }

}
