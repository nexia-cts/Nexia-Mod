package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class BwReloadShopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("bwreloadshop").executes(BwReloadShopCommand::run)
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.dev.bwreloadshop", 3)));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        boolean success = BwLoadShop.loadBedWarsShop(false);

        if (success) {
            String message = LegacyChatFormat.brandColor1 + "Reloaded Bedwars shop successfully!";
            context.getSource().sendSuccess(new TextComponent(message), false);

        } else {
            String message = LegacyChatFormat.brandColor2 + "Failed to reload Bedwars shop.";
            context.getSource().sendFailure(new TextComponent(message));
        }

        return 1;
    }

}
