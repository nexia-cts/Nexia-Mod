package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.players.BwTeam;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class BwReloadTeamColorsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("bwreloadteamcolors").executes(BwReloadTeamColorsCommand::run)
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.dev.bwreloadteamcolors", 3)));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        BwTeam.reloadPlayerTeamColors(player);

        return 1;
    }

}
