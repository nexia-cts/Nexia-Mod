package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.uhc.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class ProtectionMapCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("protectionmap")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.dev.protectionmap", 4))

                .then(Commands.literal("bedwars").executes(ProtectionMapCommand::bedwars))
                .then(Commands.literal("ffa")
                        .then(Commands.literal("uhc").executes(ProtectionMapCommand::ffa_uhc))
                        .then(Commands.literal("sky").executes(ProtectionMapCommand::ffa_sky))
                )
        );
    }

    public static int bedwars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BwAreas.createProtectionMap(player);
        return 1;
    }

    public static int ffa_uhc(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        FfaAreas.createProtectionMap(player);
        return 1;
    }

    public static int ffa_sky(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        com.nexia.ffa.sky.utilities.FfaAreas.createProtectionMap(player);
        return 1;
    }
}
