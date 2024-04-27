package com.nexia.core.commands.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.FfaGameMode;
import com.nexia.ffa.classic.utilities.player.PlayerDataManager;
import com.nexia.ffa.classic.utilities.player.SavedPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.text.DecimalFormat;

public class StatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("stats").executes(StatsCommand::run)
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("gamemode", StringArgumentType.greedyString())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((LobbyUtil.statsGameModes), builder)))
                                .executes(context -> StatsCommand.other(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "gamemode")))))
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer mcPlayer = context.getSource().getPlayerOrException();
        PlayerData executerData = com.nexia.core.utilities.player.PlayerDataManager.get(mcPlayer);
        Player player = PlayerUtil.getFactoryPlayer(mcPlayer);


        Component start = Component.text("  »").color(NamedTextColor.GRAY);

        Component user = start
                .append(Component.text(" User: ").color(ChatFormat.brandColor2))
                        .append(Component.text(player.getRawName()).color(ChatFormat.normalColor))
                ;



        Component message;

        if(executerData.gameMode == PlayerGameMode.FFA){

            message = ChatFormat.separatorLine("FFA Classic Stats");
            SavedPlayerData data = PlayerDataManager.get(mcPlayer).savedData;

            int kills = data.kills;
            int deaths = data.deaths;
            int killstreak = data.killstreak;
            int bestKillstreak = data.bestKillstreak;

            if(executerData.ffaGameMode == FfaGameMode.KITS) {
                message = ChatFormat.separatorLine("Kit FFA Stats");
                com.nexia.ffa.kits.utilities.player.SavedPlayerData kData = com.nexia.ffa.kits.utilities.player.PlayerDataManager.get(mcPlayer).savedData;
                kills = kData.kills;
                deaths = kData.deaths;
                killstreak = kData.killstreak;
                bestKillstreak = kData.bestKillstreak;
            }

            if(executerData.ffaGameMode == FfaGameMode.UHC) {
                message = ChatFormat.separatorLine("UHC FFA Stats");
                com.nexia.ffa.uhc.utilities.player.SavedPlayerData kData = com.nexia.ffa.uhc.utilities.player.PlayerDataManager.get(mcPlayer).savedData;
                kills = kData.kills;
                deaths = kData.deaths;
                killstreak = kData.killstreak;
                bestKillstreak = kData.bestKillstreak;
            }

            if(executerData.ffaGameMode == FfaGameMode.SKY) {
                message = ChatFormat.separatorLine("Sky FFA Stats");
                com.nexia.ffa.sky.utilities.player.SavedPlayerData kData = com.nexia.ffa.sky.utilities.player.PlayerDataManager.get(mcPlayer).savedData;
                kills = kData.kills;
                deaths = kData.deaths;
                killstreak = kData.killstreak;
                bestKillstreak = kData.bestKillstreak;
            }

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                            .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                                    .append(Component.text(kills).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Deaths: ").color(ChatFormat.brandColor2))
                    .append(Component.text(deaths).color(ChatFormat.failColor))
            );

            player.sendMessage(start
                    .append(Component.text(" KDR: ").color(ChatFormat.brandColor2))
                            .append(Component.text(calculateKDR(kills, deaths)).color(ChatFormat.greenColor))
            );

            player.sendMessage(start
                            .append(Component.text(" Killstreak: ").color(ChatFormat.brandColor2))
                                    .append(Component.text(killstreak).color(ChatFormat.goldColor))
                                            .append(Component.text("/").color(ChatFormat.arrowColor))
                                                    .append(Component.text(bestKillstreak).color(ChatFormat.goldColor))
            );
        }

        if(executerData.gameMode == PlayerGameMode.LOBBY){
            message = ChatFormat.separatorLine("Duels Stats");
            com.nexia.minigames.games.duels.util.player.SavedPlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(mcPlayer).savedData;
            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
        }


        if(executerData.gameMode == PlayerGameMode.BEDWARS){
            message = ChatFormat.separatorLine("Bedwars Stats");
            com.nexia.minigames.games.bedwars.util.player.SavedPlayerData data = com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.get(mcPlayer).savedData;
            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Beds broken: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.bedsBroken).color(ChatFormat.failColor))
            );
        }

        if(executerData.gameMode == PlayerGameMode.OITC){
            message = ChatFormat.separatorLine("OITC Stats");
            com.nexia.minigames.games.oitc.util.player.SavedPlayerData data = com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(mcPlayer).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.kills).color(ChatFormat.failColor))
            );
        }

        if(executerData.gameMode == PlayerGameMode.FOOTBALL){
            message = ChatFormat.separatorLine("Football Stats");
            com.nexia.minigames.games.football.util.player.SavedPlayerData data = com.nexia.minigames.games.football.util.player.PlayerDataManager.get(mcPlayer).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Goals: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.goals).color(ChatFormat.failColor))
            );
        }

        if(executerData.gameMode == PlayerGameMode.SKYWARS){
            message = ChatFormat.separatorLine("SkyWars Stats");
            com.nexia.minigames.games.skywars.util.player.SavedPlayerData data = com.nexia.minigames.games.skywars.util.player.PlayerDataManager.get(mcPlayer).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.losses).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.kills).color(ChatFormat.failColor))
            );
        }
        player.sendMessage(ChatFormat.separatorLine(null));
        return 1;
    }

    public static float calculateKDR(int kills, int deaths){

        if(deaths <= 0 || kills <= 0) { return 0; }
        return Float.parseFloat(new DecimalFormat("#.##").format((float) kills / deaths));
    }

    public static int other(CommandContext<CommandSourceStack> context, ServerPlayer otherPlayer, String gamemode) throws CommandSyntaxException {
        ServerPlayer mcPlayer = context.getSource().getPlayerOrException();
        Player player = PlayerUtil.getFactoryPlayer(mcPlayer);

        Component start = Component.text("  »").color(NamedTextColor.GRAY);

        Component user = start
                .append(Component.text(" User: ").color(ChatFormat.brandColor2))
                .append(Component.text(otherPlayer.getScoreboardName()).color(ChatFormat.normalColor))
                ;



        Component message;

        if(gamemode.equalsIgnoreCase("ffa classic") || gamemode.equalsIgnoreCase("kit ffa") || gamemode.equalsIgnoreCase("sky ffa") || gamemode.equalsIgnoreCase("uhc ffa")){
            message = ChatFormat.separatorLine("FFA Classic Stats");
            SavedPlayerData data = PlayerDataManager.get(otherPlayer).savedData;

            int kills = data.kills;
            int deaths = data.deaths;
            int killstreak = data.killstreak;
            int bestKillstreak = data.bestKillstreak;

            if(gamemode.equalsIgnoreCase("kit ffa")) {
                message = ChatFormat.separatorLine("Kit FFA Stats");
                com.nexia.ffa.kits.utilities.player.SavedPlayerData kData = com.nexia.ffa.kits.utilities.player.PlayerDataManager.get(otherPlayer).savedData;
                kills = kData.kills;
                deaths = kData.deaths;
                killstreak = kData.killstreak;
                bestKillstreak = kData.bestKillstreak;
            }

            if(gamemode.equalsIgnoreCase("sky ffa")) {
                message = ChatFormat.separatorLine("Sky FFA Stats");
                com.nexia.ffa.sky.utilities.player.SavedPlayerData kData = com.nexia.ffa.sky.utilities.player.PlayerDataManager.get(otherPlayer).savedData;
                kills = kData.kills;
                deaths = kData.deaths;
                killstreak = kData.killstreak;
                bestKillstreak = kData.bestKillstreak;
            }

            if(gamemode.equalsIgnoreCase("uhc ffa")) {
                message = ChatFormat.separatorLine("UHC FFA Stats");
                com.nexia.ffa.uhc.utilities.player.SavedPlayerData kData = com.nexia.ffa.uhc.utilities.player.PlayerDataManager.get(otherPlayer).savedData;
                kills = kData.kills;
                deaths = kData.deaths;
                killstreak = kData.killstreak;
                bestKillstreak = kData.bestKillstreak;
            }

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(kills).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Deaths: ").color(ChatFormat.brandColor2))
                    .append(Component.text(deaths).color(ChatFormat.failColor))
            );

            player.sendMessage(start
                    .append(Component.text(" KDR: ").color(ChatFormat.brandColor2))
                    .append(Component.text(calculateKDR(kills, deaths)).color(ChatFormat.greenColor))
            );

            player.sendMessage(start
                    .append(Component.text(" Killstreak: ").color(ChatFormat.brandColor2))
                    .append(Component.text(killstreak).color(ChatFormat.goldColor))
                    .append(Component.text("/").color(ChatFormat.arrowColor))
                    .append(Component.text(bestKillstreak).color(ChatFormat.goldColor))
            );
        }

        if(gamemode.equalsIgnoreCase("duels")){
            message = ChatFormat.separatorLine("Duels Stats");
            com.nexia.minigames.games.duels.util.player.SavedPlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(otherPlayer).savedData;
            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
        }


        if(gamemode.equalsIgnoreCase("bedwars")){
            message = ChatFormat.separatorLine("Bedwars Stats");
            com.nexia.minigames.games.bedwars.util.player.SavedPlayerData data = com.nexia.minigames.games.bedwars.util.player.PlayerDataManager.get(otherPlayer).savedData;
            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Beds broken: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.bedsBroken).color(ChatFormat.failColor))
            );
        }

        if(gamemode.equalsIgnoreCase("oitc")){
            message = ChatFormat.separatorLine("OITC Stats");
            com.nexia.minigames.games.oitc.util.player.SavedPlayerData data = com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(otherPlayer).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.kills).color(ChatFormat.failColor))
            );
        }

        if(gamemode.equalsIgnoreCase("football")){
            message = ChatFormat.separatorLine("Football Stats");
            com.nexia.minigames.games.football.util.player.SavedPlayerData data = com.nexia.minigames.games.football.util.player.PlayerDataManager.get(otherPlayer).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.loss).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Goals: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.goals).color(ChatFormat.failColor))
            );
        }

        if(gamemode.equalsIgnoreCase("skywars")){
            message = ChatFormat.separatorLine("SkyWars Stats");
            com.nexia.minigames.games.skywars.util.player.SavedPlayerData data = com.nexia.minigames.games.skywars.util.player.PlayerDataManager.get(otherPlayer).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.wins).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.losses).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.kills).color(ChatFormat.failColor))
            );
        }
        player.sendMessage(ChatFormat.separatorLine(null));

        return 1;
    }
}
