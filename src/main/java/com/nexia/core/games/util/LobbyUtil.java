package com.nexia.core.games.util;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.player.GamemodeBanHandler;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.world.WorldUtil;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.football.FootballGameMode;
import com.nexia.minigames.games.football.util.player.FootballPlayerData;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcGameMode;
import com.nexia.minigames.games.oitc.util.player.OITCPlayerData;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import com.nexia.minigames.games.skywars.util.player.SkywarsPlayerData;
import com.nexia.nexus.api.world.World;
import com.nexia.nexus.api.world.nbt.NBTObject;
import com.nexia.nexus.api.world.nbt.NBTValue;
import com.nexia.nexus.api.world.types.Minecraft;
import com.nexia.nexus.api.world.util.Location;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyori.adventure.text.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static com.nexia.core.utilities.player.BanHandler.getBanTime;

public class LobbyUtil {

    public static String[] statsGameModes = {"FFA CLASSIC", "SKY FFA", "UHC FFA", "KIT FFA", "BEDWARS", "OITC", "DUELS", "SKYWARS", "FOOTBALL"};

    public static ServerLevel lobbyWorld = null;

    public static World nexusLobbyWorld = null;
    public static Location nexusLobbyLocation = null;

    public static EntityPos lobbySpawn = new EntityPos(0.5, 65, 0.5, 0, 0);

    public static boolean isLobbyWorld(Level level) {
        return level.dimension() == Level.OVERWORLD;
    }

    public static boolean isLobbyWorld(World world) {
        return world.getIdentifier().toString().equals("minecraft:overworld");
    }

    public static void setLobbyWorld(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (isLobbyWorld(level)) {
                lobbyWorld = level;
                nexusLobbyWorld = WorldUtil.getWorld(level);
                break;
            }
        }

        nexusLobbyLocation = new Location(lobbySpawn.x, lobbySpawn.y, lobbySpawn.z, lobbySpawn.yaw, lobbySpawn.pitch, nexusLobbyWorld);
    }

    // Players with the tag will not be affected by server rank teams given by server datapack
    public static final String NO_RANK_DISPLAY_TAG = "no_rank_display";

    public static final String NO_FALL_DAMAGE_TAG = "no_fall_damage";

    public static final String NO_DAMAGE_TAG = "no_damage";

    public static final String NO_SATURATION_TAG = "no_saturation";

    public static String[] removedTags = {
            PlayerGameMode.BEDWARS.tag,
            "bedwars",
            PlayerGameMode.FOOTBALL.tag,
            "in_football_game",
            "duels",
            PlayerGameMode.SKYWARS.tag,
            PlayerGameMode.OITC.tag,
            "in_oitc_game",
            "in_kitroom",
            NO_RANK_DISPLAY_TAG,
            NO_SATURATION_TAG,
            NO_FALL_DAMAGE_TAG,
            NO_DAMAGE_TAG
    };

    public static void returnToLobby(NexiaPlayer player, boolean tp) {
        player.leaveAllGames();
        player.reset(true, Minecraft.GameMode.ADVENTURE);

        // Duels shit
        player.addTag("duels");
        DuelGameHandler.leave(player, false);

        if (tp) {
            player.setRespawnPosition(nexusLobbyLocation, lobbySpawn.yaw, true, false);
            player.teleport(nexusLobbyLocation);

            if(player.hasPermission("nexia.prefix.supporter")) {
                player.setAbleToFly(true);
            }

            LobbyUtil.giveItems(player);
        }



        ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode = PlayerGameMode.LOBBY;
        if (ServerPlayNetworking.canSend(player.unwrap(), NexiaCore.CONVENTIONAL_BRIDGING_UPDATE_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(true);
            ServerPlayNetworking.send(player.unwrap(), NexiaCore.CONVENTIONAL_BRIDGING_UPDATE_ID, buf);
        }
    }

    public static void giveItems(NexiaPlayer player) {

        NBTObject hideAttrubtesNBTObject = NBTObject.create();
        hideAttrubtesNBTObject.set("HideFlags", NBTValue.of(39));

        NBTObject unbreakableNBTObject = hideAttrubtesNBTObject.copy();
        unbreakableNBTObject.set("Unbreakable", NBTValue.of(1));


        com.nexia.nexus.api.world.item.ItemStack compass = com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.COMPASS);
        compass.setItemNBT(hideAttrubtesNBTObject.copy());
        compass.setLore(Component.text("Right click to open the gamemode selector menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
        compass.setDisplayName(Component.text("Gamemode Selector", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.nexia.nexus.api.world.item.ItemStack nameTag = com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.NAME_TAG);
        nameTag.setItemNBT(hideAttrubtesNBTObject.copy());
        nameTag.setLore(Component.text("Right click to open the prefix selector menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
        nameTag.setDisplayName(Component.text("Prefix Selector", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.nexia.nexus.api.world.item.ItemStack queueSword = com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.IRON_SWORD);
        queueSword.setItemNBT(hideAttrubtesNBTObject.copy());
        queueSword.setLore(new ArrayList<>(Arrays.asList(
                Component.text("Right click to open the queue menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false),
                Component.text("Hit a player to duel them.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false)
        )));
        queueSword.setDisplayName(Component.text("Duel Sword", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.nexia.nexus.api.world.item.ItemStack teamSword = com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.IRON_AXE);
        teamSword.setItemNBT(hideAttrubtesNBTObject.copy());
        teamSword.setLore(new ArrayList<>(Arrays.asList(
                Component.text("Right click to list the team you're in.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false),
                Component.text("Hit a player to invite them to your team.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false)
        )));
        teamSword.setDisplayName(Component.text("Team Axe", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.nexia.nexus.api.world.item.ItemStack customDuelSword = com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.DIAMOND_SWORD);
        customDuelSword.setItemNBT(hideAttrubtesNBTObject.copy());
        customDuelSword.setLore(Component.text("Hit a player to duel them in your custom kit.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
        customDuelSword.setDisplayName(Component.text("Custom Duel Sword", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));

        if(player.hasPermission("nexia.prefix.supporter")) {

            com.nexia.nexus.api.world.item.ItemStack elytra = com.nexia.nexus.api.world.item.ItemStack.create(Minecraft.Item.ELYTRA);
            elytra.setItemNBT(unbreakableNBTObject.copy());
            elytra.setLore(Component.text("Thanks for supporting the server!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
            elytra.setDisplayName(Component.text("Supporter Elytra", ChatFormat.brandColor2).decorate(ChatFormat.bold).decoration(ChatFormat.italic, false));

            player.getInventory().setItemStack(38, elytra);
        }


        player.getInventory().setItemStack(0, customDuelSword); // 1st slot
        player.getInventory().setItemStack(4, compass); // middle slot
        player.getInventory().setItemStack(3, nameTag); // left
        player.getInventory().setItemStack(5, queueSword); // right
        player.getInventory().setItemStack(8, teamSword); // like right right not right

        player.refreshInventory();
    }

    public static boolean checkGameModeBan(NexiaPlayer player, String game) {
        ArrayList<PlayerGameMode> bannedGameModes = GamemodeBanHandler.getBannedGameModes(player);
        if(bannedGameModes.isEmpty()) {
            return false;
        }

        for(PlayerGameMode gameMode : bannedGameModes) {
            if(game.toLowerCase().contains(gameMode.id.toLowerCase())) {

                JSONObject banJSON = GamemodeBanHandler.getBanList(player.getUUID(), gameMode);

                if (banJSON != null) {
                    LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
                    if(LocalDateTime.now().isAfter(banTime)) {
                        GamemodeBanHandler.removeBanFromList(player.getUUID(), gameMode);
                        return false;
                    } else {
                        player.sendNexiaMessage(
                                Component.text("You are gamemode (" + gameMode.name + ") banned for ", ChatFormat.Minecraft.white)
                                        .append(Component.text(BanHandler.banTimeToText(banTime), ChatFormat.brandColor2))
                                        .append(Component.text(".\nReason: ", ChatFormat.Minecraft.white))
                                        .append(Component.text((String) banJSON.get("reason"), ChatFormat.brandColor2))
                        );
                        return true;
                    }
                } else {
                    player.sendNexiaMessage("You are gamemode (%s) banned!", gameMode.name);
                }

                LobbyUtil.returnToLobby(player, true);

                return true;
            }
        }

        return false;
    }

    public static void sendGame(NexiaPlayer player, String game, boolean message, boolean tp) {

        if (checkGameModeBan(player, game)) {
            return;
        }
        if (ServerPlayNetworking.canSend(player.unwrap(), NexiaCore.CONVENTIONAL_BRIDGING_UPDATE_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(true);
            ServerPlayNetworking.send(player.unwrap(), NexiaCore.CONVENTIONAL_BRIDGING_UPDATE_ID, buf);
        }

        for (BaseFfaUtil util : BaseFfaUtil.ffaUtils) {
            if (game.equalsIgnoreCase(util.getNameLowercase() + " ffa") && !util.canGoToSpawn(player)) {
                player.sendNexiaMessage("You must be fully healed to go to spawn!");
                return;
            }
        }

        if (player.hasTag("duels")) {
            player.removeTag("duels");
            DuelGameHandler.leave(player, true);
        }

        player.reset(true, Minecraft.GameMode.ADVENTURE);
        player.leaveAllGames();

        if (game.equalsIgnoreCase("classic ffa") ||
                game.equalsIgnoreCase("kits ffa") ||
                game.equalsIgnoreCase("pot ffa") ||
                game.equalsIgnoreCase("sky ffa") ||
                game.equalsIgnoreCase("uhc ffa")) {
            ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode = PlayerGameMode.FFA;
            if (message) { player.sendActionBarMessage(Component.text("You have joined §8🗡 §7§lFFA §b🔱")); }
        }

        for (BaseFfaUtil util : BaseFfaUtil.ffaUtils) {
            if (game.equalsIgnoreCase(util.getNameLowercase() + " ffa")) {
                player.addTag(NO_FALL_DAMAGE_TAG);
                util.wasInSpawn.add(player.getUUID());
                ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).ffaGameMode = util.getGameMode();
                if (tp) {
                    util.sendToSpawn(player);
                    player.setRespawnPosition(util.getRespawnLocation(), util.getSpawn().yaw, true, false);
                }

                util.joinOrRespawn(player, false);
                util.clearProjectiles(player);
            }
        }

        if (game.equalsIgnoreCase("classic ffa")) {
            FfaClassicUtil.INSTANCE.setInventory(player);
        }

        if (game.equalsIgnoreCase("bedwars")) {
            if(message){ player.sendActionBarMessage(Component.text("You have joined §b\uD83E\uDE93 §c§lBedwars §e⚡"));}
            BwPlayerEvents.tryToJoin(player, false);
        }

        if (game.equalsIgnoreCase("duels")) {
            LobbyUtil.returnToLobby(player, tp);

            if(message){
                player.sendActionBarMessage(Component.text("You have joined §f☯ §c§lDuels §7\uD83E\uDE93"));
                player.sendNexiaMessage("Duels has now moved here. (main hub)");
                player.sendMessage(Component.text("Meaning you can now use /duel, /queue and /spectate inside of the normal hub WITHOUT going to duels!"));
            }
        }

        if(game.equalsIgnoreCase("oitc")){
            player.addTag(OitcGame.OITC_TAG);
            ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode = PlayerGameMode.OITC;
            OitcGame.death(player, player.unwrap().getLastDamageSource());

            ((OITCPlayerData)PlayerDataManager.getDataManager(NexiaCore.OITC_DATA_MANAGER).get(player)).gameMode = OitcGameMode.LOBBY;

            OitcGame.joinQueue(player);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7\uD83D\uDDE1 §f§lOITC §7\uD83C\uDFF9"));}
        }

        if(game.equalsIgnoreCase("football")){
            player.addTag(FootballGame.FOOTBALL_TAG);
            ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode = PlayerGameMode.FOOTBALL;
            ((FootballPlayerData)PlayerDataManager.getDataManager(NexiaCore.FOOTBALL_DATA_MANAGER).get(player)).gameMode = FootballGameMode.LOBBY;

            FootballGame.joinQueue(player);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7○ §7§lFootball §7\uD83D\uDDE1"));}
        }


        if(game.equalsIgnoreCase("skywars")){
            player.addTag(PlayerGameMode.SKYWARS.tag);
            ((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player)).gameMode = PlayerGameMode.SKYWARS;
            SkywarsGame.death(player, player.unwrap().getLastDamageSource());

            ((SkywarsPlayerData)PlayerDataManager.getDataManager(NexiaCore.SKYWARS_DATA_MANAGER).get(player)).gameMode = SkywarsGameMode.LOBBY;

            SkywarsGame.joinQueue(player);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7☐ §aSkywars §7\uD83D\uDDE1"));}
        }
    }

}
