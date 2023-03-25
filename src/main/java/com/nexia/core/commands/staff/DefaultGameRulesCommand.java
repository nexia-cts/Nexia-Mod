package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.Main;
import com.nexia.core.utilities.player.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;

public class DefaultGameRulesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("defaultgamerules").executes(DefaultGameRulesCommand::run)
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.dev.defaultgamerules", 3)));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        for (String gamerule : gamerules.keySet()) {
            String value = gamerules.get(gamerule);
            Main.server.getCommands().performCommand(player.createCommandSourceStack(),
                    "gamerule " + gamerule + " " + value);
        }

        return 1;
    }

    private static final HashMap<String, String> gamerules = getGameRules();

    private static HashMap<String, String> getGameRules() {
        HashMap<String, String> gamerules = new HashMap<>();
        gamerules.put("announceAdvancements", "false");
        gamerules.put("commandBlockOutput", "false");
        gamerules.put("doDaylightCycle", "false");
        gamerules.put("doFireTick", "false");
        gamerules.put("doImmediateRespawn", "true");
        gamerules.put("doMobSpawning", "false");
        gamerules.put("doPatrolSpawning", "false");
        gamerules.put("doTraderSpawning", "false");
        gamerules.put("doWeatherCycle", "false");
        gamerules.put("keepInventory", "true");
        gamerules.put("mobGriefing", "false");
        gamerules.put("randomTickSpeed", "0");
        gamerules.put("showDeathMessages", "false");
        return gamerules;
    }

}
