package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class RankCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("rank")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.rank", 4))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("rank", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Main.config.ranks), builder)))
                                .executes(context -> RankCommand.give(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "rank")))))
        );
    }

    public static int give(CommandContext<CommandSourceStack> context, ServerPlayer player, String rank) {
        CommandSourceStack executer = context.getSource();

        for(int i = 0; i < 8; i++){
            if(rank.equalsIgnoreCase(Main.config.ranks[i])){
                executer.sendSuccess(ChatFormat.format("{b1}You have set the rank of {b2}{} {b1}to: {b2}{b}{}{b1}.", player.getScoreboardName(), Main.config.ranks[i]), false);
                player.sendMessage(ChatFormat.format("{b1}Your rank has been set to: {b2}{b}{}{b1}.", Main.config.ranks[i]), Util.NIL_UUID);

                for (int i2 = 0; i2 < 8; i2++) {
                    player.removeTag(Main.config.ranks[i2]);
                }

                PlayerUtil.executeServerCommand("/lp user %player% parent set " + Main.config.ranks[i], player);
                player.addTag(Main.config.ranks[i]);
            }
        }

        return 1;
    }
}
