package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

public class DuelsDeleteWorldsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("duelsdeleteworld").executes(DuelsDeleteWorldsCommand::run)
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.dev.duelsdeleteworld")));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {


        for(ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            String[] split = level.dimension().toString().replaceAll("]", "").split(":");
            if (split[1].toLowerCase().contains("duels") && !split[2].toLowerCase().contains("hub")) {
                GamemodeHandler.deleteWorld(split[2]);
            }
        }

        context.getSource().sendSuccess(new TextComponent("Tried to delete all temporary duel worlds."), true);
        return 1;
    }
}
