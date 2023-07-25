package com.nexia.core.commands.player;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class ShoutCommand {

    private static HashMap<UUID, Long> cooldownTime = new HashMap<>();
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("shout")
                        .requires(context -> {
                            try {
                                return Permissions.check(context.getPlayerOrException(), "nexia.prefix.supporter");
                            } catch (Exception ignored) { }
                            return false;
                        })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> ShoutCommand.shout(context, StringArgumentType.getString(context, "message")))
                )
        );
    }

    public static String timeToText(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        minutes %= 60;

        return minutes + "m, " + seconds + "s";
    }


    public static int shout(CommandContext<CommandSourceStack> context, String message) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        Player factoryExecutor = PlayerUtil.getFactoryPlayer(executor);
        if(ShoutCommand.cooldownTime.get(executor.getUUID()) == null) ShoutCommand.cooldownTime.put(executor.getUUID(), System.currentTimeMillis());
        long longTime = ShoutCommand.cooldownTime.get(executor.getUUID());

        if(longTime - System.currentTimeMillis() > 0) {
            String time = ShoutCommand.timeToText(longTime - System.currentTimeMillis());
            factoryExecutor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("You are still on cooldown, you have ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(time).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                            .append(Component.text(" left!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
            ));
            return 0;
        }

        Component cmessage = Component.text(factoryExecutor.getRawName()).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true)
                .append(Component.text( " shouts: " + message).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                );

        for(Player player : ServerTime.factoryServer.getPlayers()) {
            player.sendMessage(cmessage);
        }

        ShoutCommand.cooldownTime.remove(executor.getUUID());
        ShoutCommand.cooldownTime.put(executor.getUUID(),  System.currentTimeMillis() + 300000);

        return 1;
    }
}
