package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.minigames.games.bedwars.shop.BwLoadShop;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class BwReloadShopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("bwreloadshop").executes(BwReloadShopCommand::run)
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.dev.bwreloadshop", 3)));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        boolean success = BwLoadShop.loadBedWarsShop(false) &&
                BwLoadShop.loadBedWarsShop(false);

        if (success) {
            String message = ChatFormat.brandColor1 + "Reloaded Bedwars shop successfully!";
            context.getSource().sendSuccess(new TextComponent(message), false);

        } else {
            String message = ChatFormat.brandColor2 + "Failed to reload Bedwars shop.";
            context.getSource().sendFailure(new TextComponent(message));
        }

        return 1;
    }

}
