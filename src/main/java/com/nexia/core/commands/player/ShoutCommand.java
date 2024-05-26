package com.nexia.core.commands.player;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.command.CommandUtils;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.misc.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.UUID;

public class ShoutCommand {

    private static final HashMap<UUID, Long> cooldownTime = new HashMap<>();
    
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("shout")
                        .requires(context -> {
                            try {
                                return CommandUtil.hasPermission(context, "nexia.prefix.supporter");
                            } catch (Exception ignored) { }
                            return false;
                        })
                .then(CommandUtils.argument("message", StringArgumentType.greedyString())
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


    public static int shout(CommandContext<CommandSourceInfo> context, String message) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        NexiaPlayer executor = CommandUtil.getPlayer(context);

        if (PlayerMutes.muted(executor)) return 0;

        if(ShoutCommand.cooldownTime.get(executor.getUUID()) == null) ShoutCommand.cooldownTime.put(executor.getUUID(), System.currentTimeMillis());
        long longTime = ShoutCommand.cooldownTime.get(executor.getUUID());

        if(longTime - System.currentTimeMillis() > 0) {
            String time = ShoutCommand.timeToText(longTime - System.currentTimeMillis());
            executor.sendMessage(ChatFormat.nexiaMessage.append(
                    Component.text("You are still on cooldown, you have ").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                            .append(Component.text(time).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                            .append(Component.text(" left!").color(ChatFormat.normalColor).decoration(ChatFormat.bold, false))
            ));
            return 0;
        }

        Component cmessage = Component.text(executor.getRawName()).color(ChatFormat.brandColor1).decoration(ChatFormat.bold, true)
                .append(Component.text( " shouts: " + message).color(ChatFormat.normalColor).decoration(ChatFormat.bold, false)
                );

        for(Player player : ServerTime.metisServer.getPlayers()) {
            player.sendMessage(cmessage);
        }

        ShoutCommand.cooldownTime.remove(executor.getUUID());
        ShoutCommand.cooldownTime.put(executor.getUUID(),  System.currentTimeMillis() + 300000);

        return 1;
    }
}
