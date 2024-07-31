package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.minigames.games.bedwars.players.BedwarsTeam;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BwReloadTeamColorsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("bwreloadteamcolors")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.dev.bwreloadteamcolors", 3))
                .executes(BwReloadTeamColorsCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BedwarsTeam.reloadPlayerTeamColors();
        return 1;
    }

}
