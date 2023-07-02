package com.nexia.core.games.util;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.utilities.FfaAreas;
import com.nexia.ffa.utilities.FfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.DuelsGame;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcGameMode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
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

    public static void leaveAllGames(ServerPlayer minecraftPlayer, boolean tp) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        if (BwUtil.isInBedWars(minecraftPlayer)) BwPlayerEvents.leaveInBedWars(minecraftPlayer);
        else if (FfaUtil.isFfaPlayer(minecraftPlayer)) {
            FfaUtil.leaveOrDie(minecraftPlayer, minecraftPlayer.getLastDamageSource(), true);
        }
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.LOBBY) DuelGameHandler.leave(minecraftPlayer, false);
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.OITC) OitcGame.leave(minecraftPlayer);

        BwScoreboard.removeScoreboardFor(minecraftPlayer);

        for(int i = 0; i < LobbyUtil.removedTags.length; i++){
            if(player.hasTag(LobbyUtil.removedTags[i])){
                player.removeTag(LobbyUtil.removedTags[i]);
            }
        }

        returnToLobby(minecraftPlayer, tp);
    }

    public static void returnToLobby(ServerPlayer minecraftPlayer, boolean tp) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        for(int i = 0; i < LobbyUtil.removedTags.length; i++){
            if(player.hasTag(LobbyUtil.removedTags[i])){
                player.removeTag(LobbyUtil.removedTags[i]);
            }
        }

        //minecraftPlayer.setInvulnerable(false);

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        // Duels shit
        player.addTag("duels");
        DuelGameHandler.leave(minecraftPlayer, false);

        if (tp) {
            minecraftPlayer.setRespawnPosition(lobbyWorld.dimension(), lobbySpawn.toBlockPos(), lobbySpawn.yaw, true, false);
            minecraftPlayer.teleportTo(lobbyWorld, lobbySpawn.x, lobbySpawn.y, lobbySpawn.z, lobbySpawn.pitch, lobbySpawn.yaw);

            if(Permissions.check(minecraftPlayer, "nexia.prefix.supporter")) {
                player.setAbleToFly(true);
            }

            LobbyUtil.giveItems(minecraftPlayer);
        }

        minecraftPlayer.connection.send(new ClientboundStopSoundPacket());
        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(minecraftPlayer);

        PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.LOBBY;
        player.removeTag(LobbyUtil.NO_RANK_DISPLAY_TAG);
        player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
        player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
    }

    public static void giveItems(ServerPlayer minecraftPlayer) {
        ItemStack compass = new ItemStack(Items.COMPASS);
        compass.setHoverName(new TextComponent("§eGamemode Selector"));
        ItemDisplayUtil.addGlint(compass);
        ItemDisplayUtil.addLore(compass, "§7Right click to open the menu.", 0);

        ItemStack nameTag = new ItemStack(Items.NAME_TAG);
        nameTag.setHoverName(new TextComponent("§ePrefix Selector"));
        ItemDisplayUtil.addGlint(nameTag);
        ItemDisplayUtil.addLore(nameTag, "§7Right click to open the menu.", 0);

        ItemStack queueSword = new ItemStack(Items.IRON_SWORD);
        queueSword.setHoverName(new TextComponent("§eQueue Sword"));
        ItemDisplayUtil.addGlint(queueSword);
        ItemDisplayUtil.addLore(queueSword, "§7Right click to queue menu.", 0);

        minecraftPlayer.setSlot(4, compass); //middle slot
        minecraftPlayer.setSlot(3, nameTag); //left
        minecraftPlayer.setSlot(5, queueSword); //right
        ItemStackUtil.sendInventoryRefreshPacket(minecraftPlayer);
    }

    public static void sendGame(ServerPlayer minecraftPlayer, String game, boolean message, boolean tp){
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        minecraftPlayer.setInvulnerable(false);
        player.setAbleToFly(false);
        if (!LobbyUtil.isLobbyWorld(minecraftPlayer.getLevel())) {
            LobbyUtil.leaveAllGames(minecraftPlayer, false);
        } else{
            PlayerUtil.resetHealthStatus(player);
            minecraftPlayer.setGameMode(GameType.ADVENTURE);

            player.getInventory().clear();
            minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
            minecraftPlayer.getEnderChestInventory().clearContent();

            minecraftPlayer.connection.send(new ClientboundStopSoundPacket());

            player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
            player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
            player.removeTag("duels");
            player.removeTag(LobbyUtil.NO_SATURATION_TAG);
        }
        if(game.equalsIgnoreCase("classic ffa")){
            player.addTag("ffa");
            FfaUtil.wasInSpawn.add(player.getUUID());
            PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.FFA;
            if(tp){
                minecraftPlayer.teleportTo(FfaAreas.ffaWorld, FfaAreas.spawn.x, FfaAreas.spawn.y, FfaAreas.spawn.z, FfaAreas.spawn.yaw, FfaAreas.spawn.pitch);
                minecraftPlayer.setRespawnPosition(FfaAreas.ffaWorld.dimension(), FfaAreas.spawn.toBlockPos(), FfaAreas.spawn.yaw, true, false);
            }

            FfaUtil.clearThrownTridents(minecraftPlayer);
            if(message){ player.sendActionBarMessage(Component.text("You have joined §8🗡 §7§lFFA §b🔱")); }
            FfaUtil.setInventory(minecraftPlayer);
        }
        if(game.equalsIgnoreCase("bedwars")){
            if(message){ player.sendActionBarMessage(Component.text("You have joined §b\uD83E\uDE93 §c§lBedwars §e⚡"));}
            BwPlayerEvents.tryToJoin(minecraftPlayer, false);
        }


        if(game.equalsIgnoreCase("duels")){
            LobbyUtil.leaveAllGames(minecraftPlayer, tp);
            if(message){
                player.sendActionBarMessage(Component.text("You have joined §f☯ §c§lDuels §7\uD83E\uDE93"));
                player.sendMessage(
                        ChatFormat.nexiaMessage
                                .append(Component.text("Duels has now moved here. (main hub)").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                );
                player.sendMessage(Component.text("Meaning you can now use /duel, /queue and /spectate inside of the normal hub WITHOUT going to duels!").decoration(ChatFormat.bold, false));
            }
        }

        if(game.equalsIgnoreCase("oitc")){
            player.addTag("oitc");
            PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.OITC;
            OitcGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());
            com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(minecraftPlayer).gameMode = OitcGameMode.LOBBY;
            /*
            if(tp){
                minecraftPlayer.teleportTo(OitcSpawn.oitcWorld, OitcSpawn.spawn.x, OitcSpawn.spawn.y, OitcSpawn.spawn.z, OitcSpawn.spawn.yaw, OitcSpawn.spawn.pitch);
                minecraftPlayer.setRespawnPosition(OitcSpawn.oitcWorld.dimension(), OitcSpawn.spawn.toBlockPos(), OitcSpawn.spawn.yaw, true, false);
            }

             */

            OitcGame.joinQueue(minecraftPlayer);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7\uD83D\uDDE1 §f§lOITC §7\uD83C\uDFF9"));}
        }
    }

}
