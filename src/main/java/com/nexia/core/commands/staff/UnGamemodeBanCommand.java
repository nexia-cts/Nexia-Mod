package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.player.*;
import com.nexia.core.utilities.time.ServerTime;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class UnGamemodeBanCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("ungamemodeban")
                .requires(commandSourceStack -> PlayerUtil.hasPermission(commandSourceStack, "nexia.staff.ban", 3))

                .then(Commands.argument("player", EntityArgument.player())
                        .executes(UnGamemodeBanCommand::unBan)
                )
        );
    }

    public static int unBan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack sender = context.getSource();

        BanHandler.tryUnGamemodeBan(sender, EntityArgument.getPlayer(context, "player"));

        return 1;
    }

    public static void tryGamemodeBan(CommandSourceStack sender, ServerPlayer player, PlayerGameMode gameMode, int duration, String reason) {

        SavedPlayerData bannedData = PlayerDataManager.get(player).savedData;
        long currentBanTime = bannedData.muteEnd - System.currentTimeMillis();

        if (currentBanTime > 0) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player has already been banned in {f}{} for {f}{}{s}." +
                    "\n{s}Reason: {f}{}", gameMode.name, banTimeToText(duration - System.currentTimeMillis()), reason), false);
            return;
        }

        GamemodeBanHandler.ban

        bannedData.gamemodeBanEnd = System.currentTimeMillis() + duration;
        bannedData.gamemodeBanReason = reason;
        bannedData.gamemodeBanGamemode = gameMode.id;

        ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");
    }

    public static void tryUnGamemodeBan(CommandSourceStack sender, ServerPlayer player) {

        SavedPlayerData bannedData = PlayerDataManager.get(player).savedData;
        long currentBanTime = bannedData.muteEnd - System.currentTimeMillis();

        if (currentBanTime > 0) {
            sender.sendSuccess(LegacyChatFormat.format("{s}This player has already been banned in {f}{} for {f}{}{s}." +
                    "\n{s}Reason: {f}{}", bannedData.gamemodeBanGamemode, banTimeToText(bannedData.gamemodeBanEnd - System.currentTimeMillis()), reason), false);
            return;
        }

        bannedData.gamemodeBanEnd = System.currentTimeMillis() + bannedData.gamemodeBanEnd;
        bannedData.gamemodeBanReason = bannedData.gamemodeBanReason;
        bannedData.gamemodeBanGamemode = banneddat;

        ServerTime.minecraftServer.getCommands().performCommand(player.createCommandSourceStack(), "/hub");

        sender.sendSuccess(LegacyChatFormat.format("{s}Gamemode (%s) banned {b2}{} {s}for {b2}{}{s}." +
                "\n{s}Reason: {b2}{}", gameMode.name, player.getScoreboardName(), banTimeToText(duration), reason), false);

        PlayerUtil.getFactoryPlayer(player).sendMessage(
                ChatFormat.nexiaMessage
                        .append(Component.text("You have been gamemode (" + gameMode.name + ") banned for "))
                        .append(Component.text(banTimeToText(duration)).color(ChatFormat.brandColor2))
                        .append(Component.text(".\nReason: "))
                        .append(Component.text(reason).color(ChatFormat.brandColor2))
        );
    }

}
