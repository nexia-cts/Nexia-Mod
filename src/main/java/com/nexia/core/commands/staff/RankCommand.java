package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;
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
                                .executes(context -> RankCommand.give(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "rank")))
                        )
                )
        );
    }

    public static int give(CommandContext<CommandSourceStack> context, ServerPlayer player, String rank) {
        CommandSourceStack executer = context.getSource();

        ServerPlayer mcExecutor = null;
        Player factoryExecutor = null;

        Player otherFactoryPlayer = PlayerUtil.getFactoryPlayer(player);

        try {
            mcExecutor = context.getSource().getPlayerOrException();
            factoryExecutor = PlayerUtil.getFactoryPlayer(mcExecutor);
        } catch(Exception ignored) { }

        for(int i = 0; i < 9; i++){
            if(rank.equalsIgnoreCase(Main.config.ranks[i])){
                if(factoryExecutor != null){
                    factoryExecutor.sendMessage(
                            ChatFormat.nexiaMessage()
                                            .append(Component.text("You have set the rank of ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                    .append(Component.text(otherFactoryPlayer.getRawName()).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false))
                                                            .append(Component.text(" to ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                                    .append(Component.text(Main.config.ranks[i]).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true).decoration(ChatFormat.bold, false))
                                                                            .append(Component.text(".").color(ChatFormat.brandColor2).decoration(ChatFormat.bold, false).decoration(ChatFormat.bold, false))
                    );
                } else {
                    executer.sendSuccess(LegacyChatFormat.format("{b1}You have set the rank of {b2}{} {b1}to: {b2}{b}{}{b1}.", otherFactoryPlayer.getRawName(), Main.config.ranks[i]), false);
                }


                otherFactoryPlayer.sendMessage(
                        ChatFormat.nexiaMessage()
                                        .append(Component.text("Your rank has been set to: ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                                                .append(Component.text(Main.config.ranks[i]).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                                        .append(Component.text(".").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
                );

                for (int i2 = 0; i2 < 9; i2++) {
                    otherFactoryPlayer.removeTag(Main.config.ranks[i2]);
                }

                ServerTime.factoryServer.runCommand(String.format("/lp user %s parent set %s", otherFactoryPlayer.getRawName(), Main.config.ranks[i]));

                otherFactoryPlayer.addTag(Main.config.ranks[i]);
            }
        }

        return 1;
    }
}
