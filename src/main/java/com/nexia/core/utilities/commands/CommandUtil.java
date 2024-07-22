package com.nexia.core.utilities.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.world.entity.player.Player;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandUtil {


    public static boolean hasPermission(CommandSourceInfo context, @NotNull String permission) {
        return Permissions.check(getCommandSourceStack(context, true), permission);
    }

    public static boolean hasPermission(CommandSourceInfo context, @NotNull String permission, int defaultRequiredLevel)  {
        return Permissions.check(getCommandSourceStack(context, true), permission, defaultRequiredLevel);
    }

    public static boolean hasPermission(CommandContext<CommandSourceInfo> context, @NotNull String permission) throws CommandSyntaxException {
        return hasPermission(context.getSource(), permission);
    }

    public static boolean hasPermission(CommandContext<CommandSourceInfo> context, @NotNull String permission, int defaultRequiredLevel) throws CommandSyntaxException {
        return hasPermission(context.getSource(), permission, defaultRequiredLevel);
    }

    public static CommandSourceStack getCommandSourceStack(@NotNull CommandSourceInfo info, boolean autoSetPlayer) {
        try {
            if(autoSetPlayer && info.getExecutingEntity() != null && info.getExecutingEntity() instanceof Player player) {
                return getCommandSourceStack(info, new NexiaPlayer(player));
            }
        } catch (Exception ignored) { }

        CommandSourceStack commandSourceStack = new CommandSourceStack(new CommandSource() {
            @Override
            public void sendMessage(Component component, UUID uUID) {
                info.sendMessage(ObjectMappings.convertComponent(component));
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

    public static CommandSourceStack getCommandSourceStack(@NotNull CommandSourceInfo info, NexiaPlayer player) {

        if(player == null || player.unwrap() == null) return getCommandSourceStack(info, false);

        CommandSourceStack commandSourceStack = new CommandSourceStack(new CommandSource() {
            @Override
            public void sendMessage(Component component, UUID uUID) {
                info.sendMessage(ObjectMappings.convertComponent(component));
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
