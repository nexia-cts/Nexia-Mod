package com.nexia.core.utilities.misc;

import com.combatreforged.metis.api.command.CommandSourceInfo;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandUtil {
    public static boolean checkPlayerInCommand(@NotNull CommandSourceInfo sourceInfo)  {
        return sourceInfo.getExecutingEntity() instanceof Player;
    }

    public static boolean checkPlayerInCommand(@NotNull CommandContext<CommandSourceInfo> context)  {
        return checkPlayerInCommand(context.getSource());
    }

    public static @Nullable NexiaPlayer getPlayer(CommandSourceInfo sourceInfo) {
        if(!checkPlayerInCommand(sourceInfo)) return null;
        return new NexiaPlayer((Player) sourceInfo.getExecutingEntity());
    }

    public static NexiaPlayer getPlayer(@NotNull CommandContext<CommandSourceInfo> context) {
        return getPlayer(context.getSource());
    }

    public static boolean hasPermission(CommandSourceInfo context, @NotNull String permission) {
        if(!checkPlayerInCommand(context)) return false;
        NexiaPlayer player = getPlayer(context);
        if(player == null || player.unwrap() == null) return false;

        return Permissions.check(player.unwrap(), permission);
    }

    public static boolean hasPermission(CommandSourceInfo context, @NotNull String permission, int defaultRequiredLevel) {
        if(!checkPlayerInCommand(context)) return false;
        NexiaPlayer player = getPlayer(context);
        if(player == null || player.unwrap() == null) return false;

        return Permissions.check(player.unwrap(), permission, defaultRequiredLevel);
    }

    public static boolean hasPermission(CommandContext<CommandSourceInfo> context, @NotNull String permission) {
        return hasPermission(context.getSource(), permission);
    }

    public static boolean hasPermission(CommandContext<CommandSourceInfo> context, @NotNull String permission, int defaultRequiredLevel) {
        return hasPermission(context.getSource(), permission, defaultRequiredLevel);
    }

    public static boolean failIfNoPlayerInCommand(CommandContext<CommandSourceInfo> context) {
        if(!checkPlayerInCommand(context)) {
            context.getSource().sendMessage(net.kyori.adventure.text.Component.text("A player is required to run this command here", ChatFormat.failColor));
            return true;
        }
        return false;
    }
}
