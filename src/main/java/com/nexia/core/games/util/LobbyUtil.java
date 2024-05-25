package com.nexia.core.games.util;

import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.player.*;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.ffa.kits.utilities.FfaKitsUtil;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.football.FootballGameMode;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.oitc.OitcGameMode;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsGameMode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
            NO_FALL_DAMAGE_TAG,
            NO_DAMAGE_TAG
    };

    public static void returnToLobby(NexiaPlayer player, boolean tp) {
        player.leaveAllGames();
        player.reset(true, GameType.ADVENTURE);

        // Duels shit
        player.player().get().addTag("duels");
        DuelGameHandler.leave(player, false);

        if (tp) {
            player.player().get().setRespawnPosition(lobbyWorld.dimension(), lobbySpawn.toBlockPos(), lobbySpawn.yaw, true, false);
            player.player().get().teleportTo(lobbyWorld, lobbySpawn.x, lobbySpawn.y, lobbySpawn.z, lobbySpawn.pitch, lobbySpawn.yaw);

            if(Permissions.check(player.player().get(), "nexia.prefix.supporter")) {
                player.player().get().abilities.mayfly = true;
                player.player().get().onUpdateAbilities();
            }

            LobbyUtil.giveItems(player);
        }



        PlayerDataManager.get(player).gameMode = PlayerGameMode.LOBBY;
    }

    public static void giveItems(NexiaPlayer player) {
        ItemStack compass = new ItemStack(Items.COMPASS);
        compass.setHoverName(new TextComponent("§eGamemode Selector"));
        ItemDisplayUtil.addGlint(compass);
        ItemDisplayUtil.addLore(compass, "§eRight click §7to open the menu.", 0);

        ItemStack nameTag = new ItemStack(Items.NAME_TAG);
        nameTag.setHoverName(new TextComponent("§ePrefix Selector"));
        ItemDisplayUtil.addGlint(nameTag);
        ItemDisplayUtil.addLore(nameTag, "§eRight click §7to open the menu.", 0);

        ItemStack queueSword = new ItemStack(Items.IRON_SWORD);
        queueSword.setHoverName(new TextComponent("§eDuel Sword"));
        ItemDisplayUtil.addGlint(queueSword);
        ItemDisplayUtil.addLore(queueSword, "§eRight click §7to queue menu.", 0);
        ItemDisplayUtil.addLore(queueSword, "§eHit a player §7to duel them.", 1);

        ItemStack teamSword = new ItemStack(Items.IRON_AXE);
        teamSword.setHoverName(new TextComponent("§eTeam Axe"));
        ItemDisplayUtil.addGlint(teamSword);
        ItemDisplayUtil.addLore(teamSword, "§eRight click §7to list the team.", 0);
        ItemDisplayUtil.addLore(teamSword, "§eHit a player §7to invite them to your team.", 1);

        ItemStack customDuelSword = new ItemStack(Items.DIAMOND_SWORD);
        customDuelSword.setHoverName(new TextComponent("§eCustom Duel Sword"));
        ItemDisplayUtil.addGlint(customDuelSword);
        ItemDisplayUtil.addLore(customDuelSword, "§eHit a player §7to duel them in your custom kit.", 0);

        if(Permissions.check(player.player().get(), "nexia.prefix.supporter")) {
            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.setHoverName(new TextComponent("§5§lSupporter Elytra"));
            ItemDisplayUtil.addGlint(elytra);
            elytra.getOrCreateTag().putBoolean("Unbreakable", true);
            ItemDisplayUtil.addLore(elytra, "§7Thanks for supporting the server!", 0);
            elytra.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);
            player.player().get().setItemSlot(EquipmentSlot.CHEST, elytra);
        }


        player.player().get().setSlot(0, customDuelSword); // 1st slot
        player.player().get().setSlot(4, compass); //middle slot
        player.player().get().setSlot(3, nameTag); //left
        player.player().get().setSlot(5, queueSword); //right
        player.player().get().setSlot(8, teamSword); // like right right not right

        player.refreshInventory();
    }

    public static boolean checkGameModeBan(NexiaPlayer player, String game) {
        ArrayList<PlayerGameMode> bannedGameModes = GamemodeBanHandler.getBannedGameModes(player);
        if(bannedGameModes.isEmpty()) {
            return false;
        }

        for(PlayerGameMode gameMode : bannedGameModes) {
            if(game.toLowerCase().contains(gameMode.id.toLowerCase())) {

                JSONObject banJSON = GamemodeBanHandler.getBanList(player.player().uuid, gameMode);

                if (banJSON != null) {
                    LocalDateTime banTime = getBanTime((String) banJSON.get("duration"));
                    if(LocalDateTime.now().isAfter(banTime)) {
                        GamemodeBanHandler.removeBanFromList(player.player().uuid, gameMode);
                        return false;
                    } else {
                        player.sendMessage(
                                ChatFormat.nexiaMessage
                                        .append(Component.text("You are gamemode (" + gameMode.name + ") banned for ").decoration(ChatFormat.bold, false))
                                        .append(Component.text(BanHandler.banTimeToText(banTime)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                        .append(Component.text(".\nReason: ").decoration(ChatFormat.bold, false))
                                        .append(Component.text((String) banJSON.get("reason")).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                        );
                        return true;
                    }
                } else {
                    player.sendMessage(
                            ChatFormat.nexiaMessage
                                    .append(Component.text("You are gamemode (" + gameMode.name + ") banned!"))
                    );
                }

                LobbyUtil.returnToLobby(player, true);

                return true;
            }
        }

        return false;
    }

    public static void sendGame(NexiaPlayer player, String game, boolean message, boolean tp) {

        if(checkGameModeBan(player, game)) {
            return;
        }

        if((game.equalsIgnoreCase("classic ffa") && !FfaClassicUtil.canGoToSpawn(player)) ||
                (game.equalsIgnoreCase("kits ffa") && !FfaKitsUtil.canGoToSpawn(player) ||
                        (game.equalsIgnoreCase("sky ffa") && !FfaSkyUtil.canGoToSpawn(player) ||
                                (game.equalsIgnoreCase("uhc ffa") && !FfaUhcUtil.canGoToSpawn(player))))) {

            player.sendMessage(Component.text("You must be fully healed to go to spawn!").color(ChatFormat.failColor));
            return;
        }

        if(player.getFactoryPlayer().hasTag("duels")) {
            player.getFactoryPlayer().removeTag("duels");
            DuelGameHandler.leave(player, true);
        }

        player.reset(true, GameType.ADVENTURE);
        player.leaveAllGames();

        if(game.equalsIgnoreCase("classic ffa") ||
                game.equalsIgnoreCase("kits ffa") ||
                game.equalsIgnoreCase("sky ffa") ||
                game.equalsIgnoreCase("uhc ffa")) {
            player.getFactoryPlayer().addTag(FfaUtil.FFA_TAG);
            PlayerDataManager.get(player).gameMode = PlayerGameMode.FFA;
            if(message){ player.sendActionBarMessage(Component.text("You have joined §8🗡 §7§lFFA §b🔱")); }
        }

        if(game.equalsIgnoreCase("classic ffa")){
            player.getFactoryPlayer().addTag("ffa_classic");
            FfaClassicUtil.wasInSpawn.add(player.player().uuid);
            PlayerDataManager.get(player).ffaGameMode = FfaGameMode.CLASSIC;
            if(tp){
                player.player().get().teleportTo(FfaAreas.ffaWorld, FfaAreas.spawn.x, FfaAreas.spawn.y, FfaAreas.spawn.z, FfaAreas.spawn.yaw, FfaAreas.spawn.pitch);
                player.player().get().setRespawnPosition(FfaAreas.ffaWorld.dimension(), FfaAreas.spawn.toBlockPos(), FfaAreas.spawn.yaw, true, false);
            }

            FfaClassicUtil.clearThrownTridents(player);
            FfaClassicUtil.setInventory(player);
        }

        if(game.equalsIgnoreCase("sky ffa")){
            player.player().get().addTag("ffa_sky");
            FfaSkyUtil.wasInSpawn.add(player.player().uuid);
            PlayerDataManager.get(player).ffaGameMode = FfaGameMode.SKY;
            if(tp){
                FfaSkyUtil.sendToSpawn(player);
                player.player().get().setRespawnPosition(com.nexia.ffa.sky.utilities.FfaAreas.ffaWorld.dimension(), com.nexia.ffa.sky.utilities.FfaAreas.spawn.toBlockPos(), com.nexia.ffa.sky.utilities.FfaAreas.spawn.yaw, true, false);
            }

            FfaSkyUtil.joinOrRespawn(player);
        }

        if(game.equalsIgnoreCase("uhc ffa")){
            player.getFactoryPlayer().addTag("ffa_uhc");
            FfaUhcUtil.wasInSpawn.add(player.player().uuid);
            PlayerDataManager.get(player).ffaGameMode = FfaGameMode.UHC;
            if(tp){
                FfaUhcUtil.sendToSpawn(player);
                player.player().get().setRespawnPosition(com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld.dimension(), com.nexia.ffa.uhc.utilities.FfaAreas.spawn.toBlockPos(), com.nexia.ffa.uhc.utilities.FfaAreas.spawn.yaw, true, false);
            }

            FfaUhcUtil.clearArrows(player);
            FfaUhcUtil.clearTrident(player);
        }

        if(game.equalsIgnoreCase("kits ffa")){
            player.getFactoryPlayer().addTag("ffa_kits");
            FfaKitsUtil.wasInSpawn.add(player.player().uuid);
            PlayerDataManager.get(player).ffaGameMode = FfaGameMode.KITS;
            if(tp){
                FfaKitsUtil.sendToSpawn(player);
                player.player().get().setRespawnPosition(com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld.dimension(), com.nexia.ffa.kits.utilities.FfaAreas.spawn.toBlockPos(), com.nexia.ffa.kits.utilities.FfaAreas.spawn.yaw, true, false);
            }

            FfaKitsUtil.clearThrownTridents(player);
            FfaKitsUtil.clearArrows(player);
            FfaKitsUtil.clearSpectralArrows(player);
        }

        if(game.equalsIgnoreCase("bedwars")){
            if(message){ player.sendActionBarMessage(Component.text("You have joined §b\uD83E\uDE93 §c§lBedwars §e⚡"));}
            BwPlayerEvents.tryToJoin(player, false);
        }

        if(game.equalsIgnoreCase("duels")){
            LobbyUtil.returnToLobby(player, tp);

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
            player.getFactoryPlayer().addTag(OitcGame.OITC_TAG);
            PlayerDataManager.get(player).gameMode = PlayerGameMode.OITC;
            OitcGame.death(player, player.player().get().getLastDamageSource());

            com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(player).gameMode = OitcGameMode.LOBBY;

            OitcGame.joinQueue(player);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7\uD83D\uDDE1 §f§lOITC §7\uD83C\uDFF9"));}
        }

        if(game.equalsIgnoreCase("football")){
            player.player().get().addTag(FootballGame.FOOTBALL_TAG);
            PlayerDataManager.get(player).gameMode = PlayerGameMode.FOOTBALL;
            com.nexia.minigames.games.football.util.player.PlayerDataManager.get(player).gameMode = FootballGameMode.LOBBY;

            FootballGame.joinQueue(player);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7○ §7§lFootball §7\uD83D\uDDE1"));}
        }


        if(game.equalsIgnoreCase("skywars")){
            player.player().get().addTag(PlayerGameMode.SKYWARS.tag);
            PlayerDataManager.get(player).gameMode = PlayerGameMode.SKYWARS;
            SkywarsGame.death(player, player.player().get().getLastDamageSource());

            com.nexia.minigames.games.skywars.util.player.PlayerDataManager.get(player).gameMode = SkywarsGameMode.LOBBY;

            SkywarsGame.joinQueue(player);

            if(message){player.sendActionBarMessage(Component.text("You have joined §7☐ §aSkywars §7\uD83D\uDDE1"));}
        }
    }

}
