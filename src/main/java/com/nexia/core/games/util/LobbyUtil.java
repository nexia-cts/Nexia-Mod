package com.nexia.core.games.util;

import com.nexia.core.Main;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.DuelsSpawn;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcGameMode;
import com.nexia.minigames.games.oitc.OitcSpawn;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class LobbyUtil {

    public static String[] statsGameModes = {"FFA", "BEDWARS", "OITC", "DUELS"};

    public static ServerLevel lobbyWorld = null;
    public static EntityPos lobbySpawn = new EntityPos(Main.config.lobbyPos[0], Main.config.lobbyPos[1], Main.config.lobbyPos[2], 0, 0);

    public static boolean isLobbyWorld(Level level) {
        return level.dimension() == Level.OVERWORLD;
    }

    public static void setLobbyWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isLobbyWorld(level)) {
                lobbyWorld = level;
            }
        }
    }

    // Players with the tag will not be affected by server rank teams given by server datapack
    public static final String NO_RANK_DISPLAY_TAG = "no_rank_display";

    public static final String NO_FALL_DAMAGE_TAG = "no_fall_damage";

    public static final String NO_DAMAGE_TAG = "no_damage";

    public static final String NO_SATURATION_TAG = "no_saturation";

    public static String[] removedTags = {
            "in_bedwars",
            "ffa",
            "duels",
            "oitc",
            "in_oitc_game",
            NO_RANK_DISPLAY_TAG,
            NO_SATURATION_TAG,
            NO_FALL_DAMAGE_TAG
    };

    public static void leaveAllGames(ServerPlayer player, boolean tp) {
        if (BwUtil.isInBedWars(player)) BwPlayerEvents.leaveInBedWars(player);
        else if (FfaUtil.isFfaPlayer(player)) FfaUtil.leaveOrDie(player, player.getLastDamageSource());
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.DUELS) DuelsGame.leave(player);
        else if (PlayerDataManager.get(player).gameMode == PlayerGameMode.OITC) OitcGame.leave(player);

        BwScoreboard.removeScoreboardFor(player);

        for(int i = 0; i < LobbyUtil.removedTags.length; i++){
            if(PlayerUtil.hasTag(player, LobbyUtil.removedTags[i])){
                player.removeTag(LobbyUtil.removedTags[i]);
            }
        }

        returnToLobby(player, tp);
    }

    public static void returnToLobby(ServerPlayer player, boolean tp) {
        for(int i = 0; i < LobbyUtil.removedTags.length; i++){
            if(PlayerUtil.hasTag(player, LobbyUtil.removedTags[i])){
                player.removeTag(LobbyUtil.removedTags[i]);
            }
        }

        PlayerUtil.resetHealthStatus(player);
        player.setGameMode(GameType.ADVENTURE);

        player.inventory.clearContent();
        player.inventory.setCarried(ItemStack.EMPTY);
        player.getEnderChestInventory().clearContent();

        if (tp) {
            player.setRespawnPosition(lobbyWorld.dimension(), lobbySpawn.toBlockPos(), lobbySpawn.yaw, true, false);
            lobbySpawn.teleportPlayer(lobbyWorld, player);

            ItemStack compass = new ItemStack(Items.COMPASS);
            compass.setHoverName(new TextComponent("§eGamemode Selector"));
            ItemDisplayUtil.addGlint(compass);
            ItemDisplayUtil.addLore(compass, "§7Right click to open the menu.", 0);

            ItemStack nametag = new ItemStack(Items.NAME_TAG);
            nametag.setHoverName(new TextComponent("§ePrefix Selector"));
            ItemDisplayUtil.addGlint(nametag);
            ItemDisplayUtil.addLore(nametag, "§7Right click to open the menu.", 0);

            player.setSlot(4, compass);
            player.setSlot(3, nametag);
            ItemStackUtil.sendInventoryRefreshPacket(player);
        }

        player.connection.send(new ClientboundStopSoundPacket());
        player.server.getPlayerList().sendPlayerPermissionLevel(player);

        PlayerDataManager.get(player).gameMode = PlayerGameMode.LOBBY;
        player.removeTag(LobbyUtil.NO_RANK_DISPLAY_TAG);
        player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
        player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
    }

    public static void sendGame(ServerPlayer player, String game, boolean message){
        if (!LobbyUtil.isLobbyWorld(player.level) || PlayerDataManager.get(player).gameMode != PlayerGameMode.LOBBY) {
            LobbyUtil.leaveAllGames(player, false);
        } else{
            PlayerUtil.resetHealthStatus(player);
            player.setGameMode(GameType.ADVENTURE);

            player.inventory.clearContent();
            player.inventory.setCarried(ItemStack.EMPTY);
            player.getEnderChestInventory().clearContent();

            player.connection.send(new ClientboundStopSoundPacket());

            player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_SATURATION_TAG);
        }
        if(game.equalsIgnoreCase("classic ffa")){
            player.addTag("ffa");
            FfaUtil.wasInSpawn.add(player.getUUID());
            PlayerDataManager.get(player).gameMode = PlayerGameMode.FFA;
            player.teleportTo(FfaAreas.ffaWorld, FfaAreas.spawn.x, FfaAreas.spawn.y, FfaAreas.spawn.z, FfaAreas.spawn.yaw, FfaAreas.spawn.pitch);
            player.setRespawnPosition(FfaAreas.ffaWorld.dimension(), FfaAreas.spawn.toBlockPos(), FfaAreas.spawn.yaw, true, false);
            FfaUtil.clearThrownTridents(player);
            if(message){PlayerUtil.sendActionbar(player, "You have joined §8🗡 §7§lFFA §b🔱");}
            FfaUtil.setInventory(player);
        }
        if(game.equalsIgnoreCase("bedwars")){
            if(message){PlayerUtil.sendActionbar(player, "You have joined §b\uD83E\uDE93 §c§lBedwars §e⚡");}
            BwPlayerEvents.tryToJoin(player, false);
        }

        if(game.equalsIgnoreCase("duels")){
            player.addTag("duels");
            PlayerDataManager.get(player).gameMode = PlayerGameMode.DUELS;
            GamemodeHandler.removeQueue(player, null, true);
            DuelsGame.death(player, player.getLastDamageSource());
            com.nexia.minigames.games.duels.util.player.PlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player);
            data.gameMode = DuelGameMode.LOBBY;
            data.inDuel = false;
            data.inviteKit = "";
            data.inviteMap = "";
            data.isDead = false;
            data.invitingPlayer = null;
            data.inviting = false;
            player.teleportTo(DuelsSpawn.duelWorld, DuelsSpawn.spawn.x, DuelsSpawn.spawn.y, DuelsSpawn.spawn.z, DuelsSpawn.spawn.yaw, DuelsSpawn.spawn.pitch);
            player.setRespawnPosition(DuelsSpawn.duelWorld.dimension(), DuelsSpawn.spawn.toBlockPos(), DuelsSpawn.spawn.yaw, true, false);
            if(message){PlayerUtil.sendActionbar(player, "You have joined §f☯ §c§lDuels §7\uD83E\uDE93");}
        }

        if(game.equalsIgnoreCase("oitc")){
            player.addTag("oitc");
            PlayerDataManager.get(player).gameMode = PlayerGameMode.OITC;
            OitcGame.death(player, player.getLastDamageSource());
            com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(player).gameMode = OitcGameMode.LOBBY;
            player.teleportTo(OitcSpawn.oitcWorld, OitcSpawn.spawn.x, OitcSpawn.spawn.y, OitcSpawn.spawn.z, OitcSpawn.spawn.yaw, OitcSpawn.spawn.pitch);
            player.setRespawnPosition(OitcSpawn.oitcWorld.dimension(), OitcSpawn.spawn.toBlockPos(), OitcSpawn.spawn.yaw, true, false);
            if(message){PlayerUtil.sendActionbar(player, "You have joined §7\uD83D\uDDE1 §f§lOITC §7\uD83C\uDFF9");}
        }
    }

}
