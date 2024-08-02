package com.nexia.core.commands.player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.nexia.nexus.api.world.entity.player.Player;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.UUID;

public class ShoutCommand {

    private static final HashMap<UUID, Long> cooldownTime = new HashMap<>();
    
    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("shout")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.prefix.supporter"))
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


    public static int shout(CommandContext<CommandSourceInfo> context, String message) throws CommandSyntaxException {
        NexiaPlayer executor = new NexiaPlayer(context.getSource().getPlayerOrException());

        if (PlayerMutes.muted(executor)) return 0;

        if(ShoutCommand.cooldownTime.get(executor.getUUID()) == null) ShoutCommand.cooldownTime.put(executor.getUUID(), System.currentTimeMillis());
        long longTime = ShoutCommand.cooldownTime.get(executor.getUUID());

        if(longTime - System.currentTimeMillis() > 0) {
            String time = ShoutCommand.timeToText(longTime - System.currentTimeMillis());
            executor.sendNexiaMessage(
                    Component.text("You are still on cooldown, you have ", ChatFormat.normalColor)
                            .append(Component.text(time, ChatFormat.brandColor1).decoration(ChatFormat.bold, true))
                            .append(Component.text(" left!", ChatFormat.normalColor))
            );
            return 0;
        }

        Component cmessage = Component.text(executor.getRawName(), ChatFormat.brandColor1).decoration(ChatFormat.bold, true)
                .append(Component.text( " shouts: " + message, ChatFormat.normalColor));

        for(Player player : ServerTime.nexusServer.getPlayers()) {
            player.sendMessage(cmessage);
        }

        ShoutCommand.cooldownTime.remove(executor.getUUID());
        ShoutCommand.cooldownTime.put(executor.getUUID(),  System.currentTimeMillis() + 300000);

        return 1;
    }
}
