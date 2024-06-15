package com.nexia.core.utilities.commands;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.world.entity.player.Player;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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

    public static CommandSourceStack getCommandSourceStack(CommandSourceInfo info) {

        if(checkPlayerInCommand(info) && getPlayer(info) != null) {
            return getCommandSourceStack(info, getPlayer(info));
        }

        CommandSourceStack commandSourceStack = new CommandSourceStack(new CommandSource() {
            @Override
            public void sendMessage(Component component, UUID uUID) {
            }

            @Override
            public boolean acceptsSuccess() {
                return false;
            }

            @Override
            public boolean acceptsFailure() {
                return false;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        }, new Vec3(0, 0, 0), new Vec2(0, 0), ServerTime.minecraftServer.overworld(), info.getSender().getPermissionLevel(), null, null, ServerTime.minecraftServer, null);

        return commandSourceStack;
    }

    public static CommandSourceStack getCommandSourceStack(CommandSourceInfo info, @NotNull NexiaPlayer player) {
        CommandSourceStack commandSourceStack = new CommandSourceStack(new CommandSource() {
            @Override
            public void sendMessage(Component component, UUID uUID) {
            }

            @Override
            public boolean acceptsSuccess() {
                return false;
            }

            @Override
            public boolean acceptsFailure() {
                return false;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        }, new Vec3(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()), new Vec2(player.getLocation().getPitch(), player.getLocation().getYaw()), ServerTime.minecraftServer.overworld(), info.getSender().getPermissionLevel(), player.getRawName(), new TextComponent(player.getRawName()), ServerTime.minecraftServer, player.unwrap());

        return commandSourceStack;
    }
}
