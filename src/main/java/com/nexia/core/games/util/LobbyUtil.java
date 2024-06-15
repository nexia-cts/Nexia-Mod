package com.nexia.core.games.util;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.combatreforged.factory.api.world.nbt.NBTObject;
import com.combatreforged.factory.api.world.nbt.NBTValue;
import com.combatreforged.factory.api.world.types.Minecraft;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemStackUtil;
import com.nexia.core.utilities.player.BanHandler;
import com.nexia.core.utilities.player.GamemodeBanHandler;
import com.nexia.core.utilities.player.PlayerDataManager;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwScoreboard;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.football.FootballGameMode;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcGameMode;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.nexia.core.utilities.player.BanHandler.getBanTime;

public class LobbyUtil {

    public static String[] statsGameModes = {"FFA CLASSIC", "SKY FFA", "UHC FFA", "KIT FFA", "BEDWARS", "OITC", "DUELS", "SKYWARS", "FOOTBALL"};

    public static ServerLevel lobbyWorld = null;
    public static EntityPos lobbySpawn = new EntityPos(0, 65, 0, 0, 0);

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
            PlayerGameMode.BEDWARS.tag,
            "bedwars",
            PlayerGameMode.FFA.tag,
            "ffa_classic",
            "ffa_kits",
            "ffa_sky",
            "ffa_uhc",
            PlayerGameMode.FOOTBALL.tag,
            "in_football_game",
            "duels",
            PlayerGameMode.SKYWARS.tag,
            PlayerGameMode.OITC.tag,
            "in_oitc_game",
            "in_kitroom",
            NO_RANK_DISPLAY_TAG,
            NO_SATURATION_TAG,
            NO_FALL_DAMAGE_TAG
    };

    public static void leaveAllGames(ServerPlayer minecraftPlayer, boolean tp) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        if (BwUtil.isInBedWars(minecraftPlayer)) {
            BwPlayerEvents.leaveInBedWars(minecraftPlayer);
        }
        else if (FfaUtil.isFfaPlayer(minecraftPlayer)) {
            if((FfaClassicUtil.isFfaPlayer(minecraftPlayer) && !FfaClassicUtil.canGoToSpawn(minecraftPlayer)) ||
                    (FfaKitsUtil.isFfaPlayer(minecraftPlayer)) && !FfaKitsUtil.canGoToSpawn(minecraftPlayer) ||
                            (FfaSkyUtil.isFfaPlayer(minecraftPlayer) && !FfaSkyUtil.canGoToSpawn(minecraftPlayer) ||
                                    (FfaUhcUtil.isFfaPlayer(minecraftPlayer) && !FfaUhcUtil.canGoToSpawn(minecraftPlayer)))) {

                player.sendMessage(Component.text("You must be fully healed to go to spawn!").color(ChatFormat.failColor));
                return;
            }

            FfaUtil.leaveOrDie(minecraftPlayer, minecraftPlayer.getLastDamageSource(), true);
        }
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.LOBBY) {
            DuelGameHandler.leave(minecraftPlayer, false);
        }
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.OITC) {
            OitcGame.leave(minecraftPlayer);
        }
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.SKYWARS) {
            SkywarsGame.leave(minecraftPlayer);
        }
        else if (PlayerDataManager.get(minecraftPlayer).gameMode == PlayerGameMode.FOOTBALL) {
            FootballGame.leave(minecraftPlayer);
        }

        PlayerUtil.sendBossbar(SkywarsGame.BOSSBAR, minecraftPlayer, true);
        BwScoreboard.removeScoreboardFor(minecraftPlayer);
        minecraftPlayer.setGlowing(false);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        for(String tag : LobbyUtil.removedTags) {
            if(player.hasTag(tag)) player.removeTag(tag);
        }

        returnToLobby(minecraftPlayer, tp);
    }

    public static void returnToLobby(ServerPlayer minecraftPlayer, boolean tp) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        if((FfaClassicUtil.isFfaPlayer(minecraftPlayer) && !FfaClassicUtil.canGoToSpawn(minecraftPlayer)) ||
                (FfaKitsUtil.isFfaPlayer(minecraftPlayer)) && !FfaKitsUtil.canGoToSpawn(minecraftPlayer) ||
                (FfaSkyUtil.isFfaPlayer(minecraftPlayer) && !FfaSkyUtil.canGoToSpawn(minecraftPlayer) ||
                        (FfaUhcUtil.isFfaPlayer(minecraftPlayer) && !FfaUhcUtil.canGoToSpawn(minecraftPlayer)))) {

            player.sendMessage(Component.text("You must be fully healed to go to spawn!").color(ChatFormat.failColor));
            return;
        }

        for(String tag : LobbyUtil.removedTags) {
            if(player.hasTag(tag)) player.removeTag(tag);
        }

        minecraftPlayer.setInvulnerable(false);

        minecraftPlayer.abilities.mayfly = false;
        minecraftPlayer.abilities.flying = false;
        Objects.requireNonNull(minecraftPlayer.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.0);
        minecraftPlayer.onUpdateAbilities();
        minecraftPlayer.setGlowing(false);

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();
        minecraftPlayer.setExperiencePoints(0);
        minecraftPlayer.setExperienceLevels(0);

        // Duels shit
        player.addTag("duels");
        DuelGameHandler.leave(minecraftPlayer, false);

        if (tp) {
            minecraftPlayer.setRespawnPosition(lobbyWorld.dimension(), lobbySpawn.toBlockPos(), lobbySpawn.yaw, true, false);
            minecraftPlayer.teleportTo(lobbyWorld, lobbySpawn.x, lobbySpawn.y, lobbySpawn.z, lobbySpawn.pitch, lobbySpawn.yaw);

            if(Permissions.check(minecraftPlayer, "nexia.prefix.supporter")) {
                minecraftPlayer.abilities.mayfly = true;
                minecraftPlayer.onUpdateAbilities();
            }

            LobbyUtil.giveItems(minecraftPlayer);
        }

        minecraftPlayer.connection.send(new ClientboundStopSoundPacket());

        PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.LOBBY;
        player.removeTag(LobbyUtil.NO_RANK_DISPLAY_TAG);
        player.removeTag(LobbyUtil.NO_FALL_DAMAGE_TAG);
        player.removeTag(LobbyUtil.NO_DAMAGE_TAG);
        ServerTime.minecraftServer.getPlayerList().sendPlayerPermissionLevel(minecraftPlayer);
    }

    public static void giveItems(ServerPlayer minecraftPlayer) {

        Player factoryPlayer = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        NBTObject hideAttrubtesNBTObject = NBTObject.create();
        hideAttrubtesNBTObject.set("HideFlags", NBTValue.of(39));

        NBTObject unbreakableNBTObject = hideAttrubtesNBTObject.copy();
        unbreakableNBTObject.set("Unbreakable", NBTValue.of(1));


        com.combatreforged.factory.api.world.item.ItemStack compass = com.combatreforged.factory.api.world.item.ItemStack.create(Minecraft.Item.COMPASS);
        compass.setItemNBT(hideAttrubtesNBTObject.copy());
        compass.setLore(Component.text("Right click to open the gamemode selector menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
        compass.setDisplayName(Component.text("Gamemode Selector", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.combatreforged.factory.api.world.item.ItemStack nameTag = com.combatreforged.factory.api.world.item.ItemStack.create(Minecraft.Item.NAME_TAG);
        nameTag.setItemNBT(hideAttrubtesNBTObject.copy());
        nameTag.setLore(Component.text("Right click to open the prefix selector menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
        nameTag.setDisplayName(Component.text("Prefix Selector", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.combatreforged.factory.api.world.item.ItemStack queueSword = com.combatreforged.factory.api.world.item.ItemStack.create(Minecraft.Item.IRON_SWORD);
        queueSword.setItemNBT(hideAttrubtesNBTObject.copy());
        queueSword.setLore(new ArrayList<>(Arrays.asList(
                Component.text("Right click to open the queue menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false),
                Component.text("Hit a player to duel them.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false)
        )));
        queueSword.setDisplayName(Component.text("Duel Sword", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.combatreforged.factory.api.world.item.ItemStack teamSword = com.combatreforged.factory.api.world.item.ItemStack.create(Minecraft.Item.IRON_AXE);
        teamSword.setItemNBT(hideAttrubtesNBTObject.copy());
        teamSword.setLore(new ArrayList<>(Arrays.asList(
                Component.text("Right click to list the team you're in.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false),
                Component.text("Hit a player to invite them to your team.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false)
        )));
        teamSword.setDisplayName(Component.text("Team Axe", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));


        com.combatreforged.factory.api.world.item.ItemStack customDuelSword = com.combatreforged.factory.api.world.item.ItemStack.create(Minecraft.Item.DIAMOND_SWORD);
        customDuelSword.setItemNBT(hideAttrubtesNBTObject.copy());
        customDuelSword.setLore(Component.text("Hit a player to duel them in your custom kit.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
        customDuelSword.setDisplayName(Component.text("Custom Duel Sword", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false));

        if(Permissions.check(minecraftPlayer, "nexia.prefix.supporter")) {

            com.combatreforged.factory.api.world.item.ItemStack elytra = com.combatreforged.factory.api.world.item.ItemStack.create(Minecraft.Item.ELYTRA);
            elytra.setItemNBT(unbreakableNBTObject.copy());
            elytra.setLore(Component.text("Thanks for supporting the server!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false));
            elytra.setDisplayName(Component.text("Supporter Elytra", ChatFormat.brandColor2).decorate(ChatFormat.bold).decoration(ChatFormat.italic, false));

            factoryPlayer.getInventory().setItemStack(38, elytra);
        }


        factoryPlayer.getInventory().setItemStack(0, customDuelSword); // 1st slot
        factoryPlayer.getInventory().setItemStack(4, compass); // middle slot
        factoryPlayer.getInventory().setItemStack(3, nameTag); // left
        factoryPlayer.getInventory().setItemStack(5, queueSword); // right
        factoryPlayer.getInventory().setItemStack(8, teamSword); // like right right not right

        ItemStackUtil.sendInventoryRefreshPacket(minecraftPlayer);
    }

    public static boolean checkGameModeBan(Player factoryPlayer, ServerPlayer player, String game) {
        ArrayList<PlayerGameMode> bannedGameModes = GamemodeBanHandler.getBannedGameModes(player);
        if(bannedGameModes.isEmpty()) {
            return false;
        }

        for(PlayerGameMode gameMode : bannedGameModes) {
            if(game.toLowerCase().contains(gameMode.id.toLowerCase())) {

                JSONObject banJSON = GamemodeBanHandler.getBanList(player.getStringUUID(), gameMode);

                if (banJSON != null) {
                    LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
                    if(LocalDateTime.now().isAfter(banTime)) {
                        GamemodeBanHandler.removeBanFromList(player.getStringUUID(), gameMode);
                        return false;
                    } else {
                        factoryPlayer.sendMessage(
                                ChatFormat.nexiaMessage
                                        .append(Component.text("You are gamemode (" + gameMode.name + ") banned for ").decoration(ChatFormat.bold, false))
                                        .append(Component.text(BanHandler.banTimeToText(banTime)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                        .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                                        .append(Component.text((String) banJSON.get("reason")).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                        );
                        return true;
                    }
                } else {
                    factoryPlayer.sendMessage(
                            ChatFormat.nexiaMessage
                                    .append(Component.text("You are gamemode (" + gameMode.name + ") banned!"))
                    );
                }

                factoryPlayer.runCommand("/hub");

                return true;
            }
        }

        return false;
    }

    public static void sendGame(ServerPlayer minecraftPlayer, String game, boolean message, boolean tp){
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);

        if(checkGameModeBan(player, minecraftPlayer, game)) {
            return;
        }

        minecraftPlayer.setInvulnerable(false);

        minecraftPlayer.abilities.mayfly = false;
        minecraftPlayer.onUpdateAbilities();

        if((game.equalsIgnoreCase("classic ffa") && !FfaClassicUtil.canGoToSpawn(minecraftPlayer)) ||
                (game.equalsIgnoreCase("kits ffa") && !FfaKitsUtil.canGoToSpawn(minecraftPlayer) ||
                        (game.equalsIgnoreCase("sky ffa") && !FfaSkyUtil.canGoToSpawn(minecraftPlayer) ||
                                (game.equalsIgnoreCase("uhc ffa") && !FfaUhcUtil.canGoToSpawn(minecraftPlayer))))) {

            player.sendMessage(Component.text("You must be fully healed to go to spawn!").color(ChatFormat.failColor));
            return;
        }

        if(player.hasTag("duels")) {
            player.removeTag("duels");
            DuelGameHandler.leave(minecraftPlayer, true);
        }

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

        if(game.equalsIgnoreCase("classic ffa") ||
                game.equalsIgnoreCase("kits ffa") ||
                game.equalsIgnoreCase("sky ffa") ||
                game.equalsIgnoreCase("uhc ffa")) {
            player.addTag(FfaUtil.FFA_TAG);
            PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.FFA;
            if(message){ player.sendActionBarMessage(Component.text("You have joined §8🗡 §7§lFFA §b🔱")); }
        }

        if(game.equalsIgnoreCase("classic ffa")){
            player.addTag("ffa_classic");
            FfaClassicUtil.wasInSpawn.add(player.getUUID());
            PlayerDataManager.get(minecraftPlayer).ffaGameMode = FfaGameMode.CLASSIC;
            if(tp){
                minecraftPlayer.teleportTo(FfaAreas.ffaWorld, FfaAreas.spawn.x, FfaAreas.spawn.y, FfaAreas.spawn.z, FfaAreas.spawn.yaw, FfaAreas.spawn.pitch);
                minecraftPlayer.setRespawnPosition(FfaAreas.ffaWorld.dimension(), FfaAreas.spawn.toBlockPos(), FfaAreas.spawn.yaw, true, false);
            }

            FfaClassicUtil.clearThrownTridents(minecraftPlayer);
            FfaClassicUtil.setInventory(minecraftPlayer);
        }

        if(game.equalsIgnoreCase("sky ffa")){
            player.addTag("ffa_sky");
            FfaSkyUtil.wasInSpawn.add(player.getUUID());
            PlayerDataManager.get(minecraftPlayer).ffaGameMode = FfaGameMode.SKY;
            if(tp){
                FfaSkyUtil.sendToSpawn(minecraftPlayer);
                minecraftPlayer.setRespawnPosition(com.nexia.ffa.sky.utilities.FfaAreas.ffaWorld.dimension(), com.nexia.ffa.sky.utilities.FfaAreas.spawn.toBlockPos(), com.nexia.ffa.sky.utilities.FfaAreas.spawn.yaw, true, false);
            }

            FfaSkyUtil.joinOrRespawn(minecraftPlayer);
        }

        if(game.equalsIgnoreCase("uhc ffa")){
            player.addTag("ffa_uhc");
            FfaUhcUtil.wasInSpawn.add(player.getUUID());
            PlayerDataManager.get(minecraftPlayer).ffaGameMode = FfaGameMode.UHC;
            if(tp){
                FfaUhcUtil.sendToSpawn(minecraftPlayer);
                minecraftPlayer.setRespawnPosition(com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld.dimension(), com.nexia.ffa.uhc.utilities.FfaAreas.spawn.toBlockPos(), com.nexia.ffa.uhc.utilities.FfaAreas.spawn.yaw, true, false);
            }

            FfaUhcUtil.clearArrows(minecraftPlayer);
            FfaUhcUtil.clearTrident(minecraftPlayer);
        }

        if(game.equalsIgnoreCase("kits ffa")){
            player.addTag("ffa_kits");
            FfaKitsUtil.wasInSpawn.add(player.getUUID());
            PlayerDataManager.get(minecraftPlayer).ffaGameMode = FfaGameMode.KITS;
            if(tp){
                FfaKitsUtil.sendToSpawn(minecraftPlayer);
                minecraftPlayer.setRespawnPosition(com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld.dimension(), com.nexia.ffa.kits.utilities.FfaAreas.spawn.toBlockPos(), com.nexia.ffa.kits.utilities.FfaAreas.spawn.yaw, true, false);
            }

            FfaKitsUtil.clearThrownTridents(minecraftPlayer);
            FfaKitsUtil.clearArrows(minecraftPlayer);
            FfaKitsUtil.clearSpectralArrows(minecraftPlayer);
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
            player.addTag(OitcGame.OITC_TAG);
            PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.OITC;
            OitcGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());

            com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(minecraftPlayer).gameMode = OitcGameMode.LOBBY;

            OitcGame.joinQueue(minecraftPlayer);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7\uD83D\uDDE1 §f§lOITC §7\uD83C\uDFF9"));}
        }

        if(game.equalsIgnoreCase("football")){
            player.addTag(FootballGame.FOOTBALL_TAG);
            PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.FOOTBALL;
            com.nexia.minigames.games.football.util.player.PlayerDataManager.get(minecraftPlayer).gameMode = FootballGameMode.LOBBY;

            FootballGame.joinQueue(minecraftPlayer);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7○ §7§lFootball §7\uD83D\uDDE1"));}
        }


        if(game.equalsIgnoreCase("skywars")){
            player.addTag(PlayerGameMode.SKYWARS.tag);
            PlayerDataManager.get(minecraftPlayer).gameMode = PlayerGameMode.SKYWARS;
            SkywarsGame.death(minecraftPlayer, minecraftPlayer.getLastDamageSource());

            com.nexia.minigames.games.skywars.util.player.PlayerDataManager.get(minecraftPlayer).gameMode = SkywarsGameMode.LOBBY;

            SkywarsGame.joinQueue(minecraftPlayer);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7☐ §aSkywars §7\uD83D\uDDE1"));}
        }
    }

}
