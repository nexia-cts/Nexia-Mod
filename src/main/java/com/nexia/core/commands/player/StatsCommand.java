package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerData;
import com.nexia.ffa.utilities.player.PlayerDataManager;
import com.nexia.ffa.utilities.player.SavedPlayerData;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
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

        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerData executerData = com.nexia.core.utilities.player.PlayerDataManager.get(player);
        if(executerData.gameMode == PlayerGameMode.FFA){
            String message = ChatFormat.separatorLine("FFA Stats");
            SavedPlayerData data = PlayerDataManager.get(player).savedData;
            player.sendMessage(new TextComponent(message), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Kills: §a{}", data.kills), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Deaths: §c{}", data.deaths), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}KDR: §a{}", Float.parseFloat(new DecimalFormat("#.##").format(data.kills / data.deaths))), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Killstreak: §6{}§8/§6{}", data.killstreak, data.bestKillstreak), Util.NIL_UUID);
            player.sendMessage(new TextComponent(ChatFormat.separatorLine(null)), Util.NIL_UUID);
        }

        if(executerData.gameMode == PlayerGameMode.DUELS){
            String message = ChatFormat.separatorLine("Duels Stats");
            com.nexia.minigames.games.duels.util.player.SavedPlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).savedData;
            player.sendMessage(new TextComponent(message), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Wins: §a{}", data.wins), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Losses: §c{}", data.loss), Util.NIL_UUID);
            player.sendMessage(new TextComponent(ChatFormat.separatorLine(null)), Util.NIL_UUID);
        }


        if(executerData.gameMode == PlayerGameMode.BEDWARS){
            String message = ChatFormat.separatorLine("Bedwars Stats");
            com.nexia.minigames.games.duels.util.player.SavedPlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).savedData;
            player.sendMessage(new TextComponent(message), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Wins: §a{}", data.wins), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Losses: §c{}", data.loss), Util.NIL_UUID);
            player.sendMessage(new TextComponent(ChatFormat.separatorLine(null)), Util.NIL_UUID);
        }

        if(executerData.gameMode == PlayerGameMode.OITC){
            String message = ChatFormat.separatorLine("OITC Stats");
            com.nexia.minigames.games.oitc.util.player.SavedPlayerData data = com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(player).savedData;
            player.sendMessage(new TextComponent(message), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Wins: §a{}", data.wins), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Losses: §c{}", data.loss), Util.NIL_UUID);
            player.sendMessage(ChatFormat.format("  §8» {b1}Kills: §c{}", data.kills), Util.NIL_UUID);
            player.sendMessage(new TextComponent(ChatFormat.separatorLine(null)), Util.NIL_UUID);
        }
        return 1;
    }

    public static int other(CommandContext<CommandSourceStack> context, ServerPlayer player, String gamemode) {
        CommandSourceStack executor = context.getSource();
        if(gamemode.equalsIgnoreCase("ffa")){
            String message = ChatFormat.separatorLine("FFA Stats");
            SavedPlayerData data = PlayerDataManager.get(player).savedData;
            executor.sendSuccess(new TextComponent(message), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Kills: §a{}", data.kills), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Deaths: §c{}", data.deaths), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}KDR: §a{}", Float.parseFloat(new DecimalFormat("#.##").format(data.kills / data.deaths))), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Killstreak: §6{}§8/§6{}", data.killstreak, data.bestKillstreak), false);
            executor.sendSuccess(new TextComponent(ChatFormat.separatorLine(null)), false);
        }

        if(gamemode.equalsIgnoreCase("duels")){
            String message = ChatFormat.separatorLine("Duels Stats");
            com.nexia.minigames.games.duels.util.player.SavedPlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).savedData;
            executor.sendSuccess(new TextComponent(message), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Wins: §a{}", data.wins), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Losses: §c{}", data.loss), false);
            executor.sendSuccess(new TextComponent(ChatFormat.separatorLine(null)), false);
        }


        if(gamemode.equalsIgnoreCase("bedwars")){
            String message = ChatFormat.separatorLine("Bedwars Stats");
            com.nexia.minigames.games.duels.util.player.SavedPlayerData data = com.nexia.minigames.games.duels.util.player.PlayerDataManager.get(player).savedData;
            executor.sendSuccess(new TextComponent(message), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Wins: §a{}", data.wins), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Losses: §c{}", data.loss), false);
            executor.sendSuccess(new TextComponent(ChatFormat.separatorLine(null)), false);
        }

        if(gamemode.equalsIgnoreCase("oitc")){
            String message = ChatFormat.separatorLine("OITC Stats");
            com.nexia.minigames.games.oitc.util.player.SavedPlayerData data = com.nexia.minigames.games.oitc.util.player.PlayerDataManager.get(player).savedData;
            executor.sendSuccess(new TextComponent(message), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}User: {n}{}", player.getScoreboardName()), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Wins: §a{}", data.wins), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Losses: §c{}", data.loss), false);
            executor.sendSuccess(ChatFormat.format("  §8» {b1}Kills: §c{}", data.kills), false);
            executor.sendSuccess(new TextComponent(ChatFormat.separatorLine(null)), false);
        }

        return 1;
    }
}
