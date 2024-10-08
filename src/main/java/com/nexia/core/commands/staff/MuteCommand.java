package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.PlayerMutes;
import com.nexia.core.utilities.commands.CommandUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;

public class MuteCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("mute")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.staff.mute", 1))
                .then(CommandUtils.argument("player", EntityArgument.player())
                        .then(CommandUtils.argument("duration", StringArgumentType.word())
                                .executes(context -> MuteCommand.mute(context, StringArgumentType.getString(context, "duration"), "No reason specified."))
                                .then(CommandUtils.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> MuteCommand.mute(context,
                                                StringArgumentType.getString(context, "duration"),
                                                StringArgumentType.getString(context, "reason"))
                                        )
                                )
                        )
                )
        );
    }

    public static int mute(CommandContext<CommandSourceInfo> context, String durationArg, String reason) throws CommandSyntaxException {

        CommandSourceInfo sender = context.getSource();
        ServerPlayer muted = context.getArgument("player", EntitySelector.class).findSinglePlayer(CommandUtil.getCommandSourceStack(context.getSource(), false));

        int durationInSeconds;
        try {
            durationInSeconds = parseTimeArg(durationArg);
        } catch (Exception e) {

            context.getSource().sendMessage(ChatFormat.nexiaMessage
                    .append(Component.text("Invalid duration. Examples: ", ChatFormat.normalColor))
                    .append(Component.text("1s / 2m / 3h / 4d / 5w", ChatFormat.failColor))
            );

            return 1;
        }

        PlayerMutes.mute(sender, muted, durationInSeconds, reason);

        return 1;
    }

    private static int parseTimeArg(String durationArg) throws Exception {
        StringReader stringReader = new StringReader(durationArg);
        float number = stringReader.readFloat();
        String unit = stringReader.readUnquotedString();

        int unitValue = units.getOrDefault(unit, 0);
        int time = Math.round(number * unitValue);
        if (time <= 0) {
            throw new Exception();
        }
        return time;
    }

    static HashMap<String, Integer> units = new HashMap<>();
    static {
        units.put("s", 1);
        units.put("m", 60);
        units.put("h", 3600);
        units.put("d", 86400);
        units.put("w", 604800);
    }

}
