package com.nexia.core.commands.staff;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexia.core.Main;
import com.nexia.core.commands.player.PrefixCommand;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class StaffPrefixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {

        dispatcher.register(Commands.literal("staffprefix")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.prefix", 3))
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"set", "add", "remove"}), builder)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("prefix", StringArgumentType.string())
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((Main.config.ranks), builder)))
                                        .executes(context -> {

                                            CommandSourceStack executor = context.getSource();

                                            String type = StringArgumentType.getString(context, "type");
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            String prefix = StringArgumentType.getString(context, "prefix");

                                            if(type.equalsIgnoreCase("set")){
                                                for(int i = 0; i < 8; i++){
                                                    if(prefix.equalsIgnoreCase(Main.config.ranks[i])){
                                                        executor.sendSuccess(ChatFormat.format("{b1}You have set the prefix of {b2}{} {b1}to: {b2}{b}{}{b1}.", player.getScoreboardName(), Main.config.ranks[i]), false);
                                                        player.sendMessage(ChatFormat.format("{b1}Your prefix has been set to: {b2}{b}{}{b1}.", Main.config.ranks[i]), Util.NIL_UUID);

                                                        for (int i2 = 0; i2 < 8; i2++) {
                                                            player.removeTag(Main.config.ranks[i2]);
                                                        }

                                                        player.addTag(Main.config.ranks[i]);
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("remove")){
                                                for(int i = 0; i < 8; i++){
                                                    if(prefix.equalsIgnoreCase(Main.config.ranks[i])){
                                                        executor.sendSuccess(ChatFormat.format("{b1}You have removed the prefix {b2}{b}{} {b1}from {b2}{}{b1}.", Main.config.ranks[i], player.getScoreboardName()), false);

                                                        PlayerUtil.executeServerCommand("/lp user %player% permission unset nexia.prefix." + Main.config.ranks[i], player);
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("add")){
                                                for(int i = 0; i < 8; i++){
                                                    if(prefix.equalsIgnoreCase(Main.config.ranks[i])){
                                                        executor.sendSuccess(ChatFormat.format("{b1}You have added the prefix {b2}{b}{} {b1}to {b2}{}{b1}.", Main.config.ranks[i], player.getScoreboardName()), false);

                                                        PlayerUtil.executeServerCommand("/lp user %player% permission set nexia.prefix." + Main.config.ranks[i] + " true", player);
                                                    }
                                                }
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                ));

    }
}
