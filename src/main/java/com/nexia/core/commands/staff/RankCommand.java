package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.ranks.NexiaRank;
import com.nexia.core.utilities.time.ServerTime;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class RankCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {

        ArrayList<String> ranks = new ArrayList<>();
        NexiaRank.ranks.forEach(rank -> ranks.add(rank.id));

        dispatcher.register(Commands.literal("rank")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.staff.rank"))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("rank", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((ranks), builder)))
                                .executes(context -> RankCommand.give(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "rank")))))
        );
    }

    public static int give(CommandContext<CommandSourceStack> context, ServerPlayer player, String rank) {
        CommandSourceStack executer = context.getSource();

        Player factoryExecutor = null;

        Player otherFactoryPlayer = PlayerUtil.getFactoryPlayer(player);

        try {
            factoryExecutor = PlayerUtil.getFactoryPlayer(context.getSource().getPlayerOrException());
        } catch(Exception ignored) { }

        for(NexiaRank tRank : NexiaRank.ranks){
            if(rank.equalsIgnoreCase(tRank.id)){

                if(factoryExecutor != null){
                    factoryExecutor.sendMessage(
                            ChatFormat.nexiaMessage
                                            .append(Component.text("You have set the rank of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                    .append(Component.text(otherFactoryPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                            .append(Component.text(" to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                    .append(Component.text(tRank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(".").color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false).decoration(ChatFormat.bold, false))
                    );
                } else {
                    executer.sendSuccess(LegacyChatFormat.format("{b1}You have set the rank of {b2}{} {b1}to: {b2}{b}{}{b1}.", otherFactoryPlayer.getRawName(), tRank.name), false);
                }


                otherFactoryPlayer.sendMessage(
                        ChatFormat.nexiaMessage
                                        .append(Component.text("Your rank has been set to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                .append(Component.text(tRank.name).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                );

                for (NexiaRank tRank2 : NexiaRank.ranks) {
                    otherFactoryPlayer.removeTag(tRank2.id);
                }

                ServerTime.factoryServer.runCommand(String.format("/lp user %s parent set %s", otherFactoryPlayer.getRawName(), tRank.id));

                otherFactoryPlayer.addTag(tRank.id);
            }
        }

        return 1;
    }
}
