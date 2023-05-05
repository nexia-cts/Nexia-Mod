package com.nexia.core.commands.staff;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                                            ServerPlayer mcOtherPlayer = EntityArgument.getPlayer(context, "player");
                                            Player otherPlayer = PlayerUtil.getFactoryPlayer(mcOtherPlayer);
                                            String prefix = StringArgumentType.getString(context, "prefix");

                                            ServerPlayer mcExecutor = null;
                                            Player factoryExecutor = null;

                                            try {
                                                mcExecutor = executor.getPlayerOrException();
                                                factoryExecutor = PlayerUtil.getFactoryPlayer(mcExecutor);
                                            } catch(Exception ignored){ }


                                            if(type.equalsIgnoreCase("set")){
                                                for(int i = 0; i < 9; i++){
                                                    if(prefix.equalsIgnoreCase(Main.config.ranks[i])){


                                                        if(factoryExecutor != null){
                                                            factoryExecutor.sendMessage(ChatFormat.returnAppendedComponent(
                                                                    ChatFormat.nexiaMessage(),
                                                                    Component.text("You have set the prefix of ").color(ChatFormat.normalColor),
                                                                    Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2),
                                                                    Component.text(" to: ").color(ChatFormat.normalColor),
                                                                    Component.text(Main.config.ranks[i]).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true),
                                                                    Component.text(".").color(ChatFormat.normalColor)
                                                            ));
                                                        } else {
                                                            executor.sendSuccess(LegacyChatFormat.format("{b1}You have set the prefix of {b2}{} {b1}to: {b2}{b}{}{b1}.", otherPlayer.getRawName(), Main.config.ranks[i]), false);
                                                        }

                                                        otherPlayer.sendMessage(ChatFormat.returnAppendedComponent(
                                                                ChatFormat.nexiaMessage(),
                                                                Component.text("Your prefix has been set to: ").color(ChatFormat.normalColor),
                                                                Component.text(Main.config.ranks[i]).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true),
                                                                Component.text(".").color(ChatFormat.normalColor)
                                                        ));

                                                        for (int i2 = 0; i2 < 8; i2++) {
                                                            otherPlayer.removeTag(Main.config.ranks[i2]);
                                                        }

                                                        otherPlayer.addTag(Main.config.ranks[i]);
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("remove")){
                                                for(int i = 0; i < 8; i++){
                                                    if(prefix.equalsIgnoreCase(Main.config.ranks[i])){

                                                        if(factoryExecutor != null){
                                                            otherPlayer.sendMessage(ChatFormat.returnAppendedComponent(
                                                                    Component.text("You have removed the prefix ").color(ChatFormat.normalColor),
                                                                    Component.text(Main.config.ranks[i]).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true),
                                                                    Component.text(" from ").color(ChatFormat.normalColor),
                                                                    Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2)
                                                            ));
                                                        } else {
                                                            executor.sendSuccess(LegacyChatFormat.format("{b1}You have removed the prefix {b2}{b}{} {b1}from {b2}{}{b1}.", Main.config.ranks[i], otherPlayer.getRawName()), false);
                                                        }


                                                        ServerTime.factoryServer.runCommand(String.format("/lp user %s permission unset nexia.prefix.%s", otherPlayer.getRawName(), Main.config.ranks[i]));
                                                    }
                                                }
                                            }

                                            if(type.equalsIgnoreCase("add")){
                                                for(int i = 0; i < 8; i++){
                                                    if(prefix.equalsIgnoreCase(Main.config.ranks[i])){
                                                        if(factoryExecutor != null){
                                                            otherPlayer.sendMessage(ChatFormat.returnAppendedComponent(
                                                                    Component.text("You have added the prefix ").color(ChatFormat.normalColor),
                                                                    Component.text(Main.config.ranks[i]).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true),
                                                                    Component.text(" to ").color(ChatFormat.normalColor),
                                                                    Component.text(otherPlayer.getRawName()).color(ChatFormat.brandColor2)
                                                            ));
                                                        } else {
                                                            executor.sendSuccess(LegacyChatFormat.format("{b1}You have added the prefix {b2}{b}{} {b1}to {b2}{}{b1}.", Main.config.ranks[i], otherPlayer.getRawName()), false);
                                                        }

                                                        ServerTime.factoryServer.runCommand(String.format("/lp user %s permission set nexia.prefix.%s true", otherPlayer.getRawName(), Main.config.ranks[i]));
                                                    }
                                                }
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                ));

    }
}
