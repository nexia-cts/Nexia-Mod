package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.base.player.SavedPlayerData;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.ffa.FfaGameMode;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.text.DecimalFormat;

public class StatsCommand {
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("stats").executes(StatsCommand::run)
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("gamemode", StringArgumentType.greedyString())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((LobbyUtil.statsGameModes), builder)))
                                .executes(context -> StatsCommand.other(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource())), StringArgumentType.getString(context, "gamemode")))))
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        NexiaPlayer player = new NexiaPlayer(context.getSource().getPlayerOrException());
        CorePlayerData playerData = (CorePlayerData) PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player);


        Component start = Component.text("  »").color(NamedTextColor.GRAY);

        Component user = start
                .append(Component.text(" User: ").color(ChatFormat.brandColor2))
                        .append(Component.text(player.getRawName()).color(ChatFormat.normalColor));



        Component message;

        if (playerData.gameMode == PlayerGameMode.FFA) {
            message = ChatFormat.separatorLine("FFA Classic Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.FFA_CLASSIC_DATA_MANAGER).get(player).savedData;

            if (playerData.ffaGameMode == FfaGameMode.KITS) {
                message = ChatFormat.separatorLine("Kit FFA Stats");
                data = PlayerDataManager.getDataManager(NexiaCore.FFA_KITS_DATA_MANAGER).get(player).savedData;
            }

            if (playerData.ffaGameMode == FfaGameMode.UHC) {
                message = ChatFormat.separatorLine("UHC FFA Stats");
                data = PlayerDataManager.getDataManager(NexiaCore.FFA_UHC_DATA_MANAGER).get(player).savedData;
            }

            if (playerData.ffaGameMode == FfaGameMode.SKY) {
                message = ChatFormat.separatorLine("Sky FFA Stats");
                data = PlayerDataManager.getDataManager(NexiaCore.FFA_SKY_DATA_MANAGER).get(player).savedData;
            }

            int kills = data.get(Integer.class, "kills");
            int deaths = data.get(Integer.class, "deaths");
            int killstreak = data.get(Integer.class, "killstreak");
            int bestKillstreak = data.get(Integer.class, "bestKillstreak");

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

        if(playerData.gameMode == PlayerGameMode.LOBBY){
            message = ChatFormat.separatorLine("Duels Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(player).savedData;
            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
        }


        if(playerData.gameMode == PlayerGameMode.BEDWARS){
            message = ChatFormat.separatorLine("Bedwars Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.BEDWARS_DATA_MANAGER).get(player).savedData;
            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Beds broken: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "bedsBroken")).color(ChatFormat.failColor))
            );
        }

        if(playerData.gameMode == PlayerGameMode.OITC){
            message = ChatFormat.separatorLine("OITC Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.OITC_DATA_MANAGER).get(player).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "kills")).color(ChatFormat.failColor))
            );
        }

        if(playerData.gameMode == PlayerGameMode.FOOTBALL){
            message = ChatFormat.separatorLine("Football Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.FOOTBALL_DATA_MANAGER).get(player).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Goals: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "goals")).color(ChatFormat.failColor))
            );
        }

        if(playerData.gameMode == PlayerGameMode.SKYWARS){
            message = ChatFormat.separatorLine("SkyWars Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.SKYWARS_DATA_MANAGER).get(player).savedData;

            player.sendMessage(message);
            player.sendMessage(user);
            player.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            player.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "kills")).color(ChatFormat.failColor))
            );
        }
        player.sendMessage(ChatFormat.separatorLine(null));
        return 1;
    }

    public static float calculateKDR(int kills, int deaths){

        if(deaths <= 0 || kills <= 0) { return 0; }
        return Float.parseFloat(new DecimalFormat("#.##").format((float) kills / deaths));
    }

    public static int other(CommandContext<CommandSourceInfo> context, ServerPlayer otherPlayer, String gamemode) {
        CommandSourceInfo source = context.getSource();

        Component start = Component.text("  »").color(ChatFormat.arrowColor);

        Component user = start
                .append(Component.text(" User: ").color(ChatFormat.brandColor2))
                .append(Component.text(otherPlayer.getScoreboardName()).color(ChatFormat.normalColor))
                ;



        Component message;

        if (gamemode.equalsIgnoreCase("ffa classic") || gamemode.equalsIgnoreCase("kit ffa") || gamemode.equalsIgnoreCase("sky ffa") || gamemode.equalsIgnoreCase("uhc ffa")){
            message = ChatFormat.separatorLine("FFA Classic Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.FFA_CLASSIC_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;

            if (gamemode.equalsIgnoreCase("kit ffa")) {
                message = ChatFormat.separatorLine("Kit FFA Stats");
                data = PlayerDataManager.getDataManager(NexiaCore.FFA_KITS_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;
            }

            if (gamemode.equalsIgnoreCase("sky ffa")) {
                message = ChatFormat.separatorLine("Sky FFA Stats");
                data = PlayerDataManager.getDataManager(NexiaCore.FFA_SKY_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;
            }

            if (gamemode.equalsIgnoreCase("uhc ffa")) {
                message = ChatFormat.separatorLine("UHC FFA Stats");
                data = PlayerDataManager.getDataManager(NexiaCore.FFA_UHC_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;
            }

            int kills = data.get(Integer.class, "kills");
            int deaths = data.get(Integer.class, "deaths");
            int killstreak = data.get(Integer.class, "killstreak");
            int bestKillstreak = data.get(Integer.class, "bestKillstreak");

            source.sendMessage(message);
            source.sendMessage(user);
            source.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(kills).color(ChatFormat.greenColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Deaths: ").color(ChatFormat.brandColor2))
                    .append(Component.text(deaths).color(ChatFormat.failColor))
            );

            source.sendMessage(start
                    .append(Component.text(" KDR: ").color(ChatFormat.brandColor2))
                    .append(Component.text(calculateKDR(kills, deaths)).color(ChatFormat.greenColor))
            );

            source.sendMessage(start
                    .append(Component.text(" Killstreak: ").color(ChatFormat.brandColor2))
                    .append(Component.text(killstreak).color(ChatFormat.goldColor))
                    .append(Component.text("/").color(ChatFormat.arrowColor))
                    .append(Component.text(bestKillstreak).color(ChatFormat.goldColor))
            );
        }

        if(gamemode.equalsIgnoreCase("duels")){
            message = ChatFormat.separatorLine("Duels Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;
            source.sendMessage(message);
            source.sendMessage(user);
            source.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
        }


        if(gamemode.equalsIgnoreCase("bedwars")){
            message = ChatFormat.separatorLine("Bedwars Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.BEDWARS_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;
            source.sendMessage(message);
            source.sendMessage(user);
            source.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Beds broken: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "bedsBroken")).color(ChatFormat.failColor))
            );
        }

        if(gamemode.equalsIgnoreCase("oitc")){
            message = ChatFormat.separatorLine("OITC Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.OITC_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;

            source.sendMessage(message);
            source.sendMessage(user);
            source.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "kills")).color(ChatFormat.failColor))
            );
        }

        if(gamemode.equalsIgnoreCase("football")){
            message = ChatFormat.separatorLine("Football Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.FOOTBALL_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;

            source.sendMessage(message);
            source.sendMessage(user);
            source.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Goals: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "goals")).color(ChatFormat.failColor))
            );
        }

        if(gamemode.equalsIgnoreCase("skywars")){
            message = ChatFormat.separatorLine("SkyWars Stats");
            SavedPlayerData data = PlayerDataManager.getDataManager(NexiaCore.SKYWARS_DATA_MANAGER).get(otherPlayer.getUUID()).savedData;

            source.sendMessage(message);
            source.sendMessage(user);
            source.sendMessage(start
                    .append(Component.text(" Wins: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "wins")).color(ChatFormat.greenColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Losses: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "losses")).color(ChatFormat.failColor))
            );
            source.sendMessage(start
                    .append(Component.text(" Kills: ").color(ChatFormat.brandColor2))
                    .append(Component.text(data.get(Integer.class, "kills")).color(ChatFormat.failColor))
            );
        }
        source.sendMessage(ChatFormat.separatorLine(null));

        return 1;
    }
}
