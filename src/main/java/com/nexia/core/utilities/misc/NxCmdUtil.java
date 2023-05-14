package com.nexia.core.utilities.misc;

import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Predicate;
public class NxCmdUtil {

    public static final String PREFIX = "minecraft.command.";

    public static void alterCommand(CommandNode<CommandSourceStack> child) {
        var name = child.getName();
        var packageName = commandPackageName(child);
        if (packageName == null || !packageName.startsWith("net.minecraft")) {
            return;
        }
        try {
            var field = CommandNode.class.getDeclaredField("requirement");
            field.setAccessible(true);
            Predicate<CommandSourceStack> original = child.getRequirement();
            field.set(child, original.or((source) -> Permissions.check(source, NxCmdUtil.PREFIX + name, false)));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    private static String commandPackageName(CommandNode<CommandSourceStack> node) {
        var command = node.getCommand();
        if (command != null) {
            return command.getClass().getPackageName();
        }
        var redirect = node.getRedirect();
        if (redirect != null) {
            return commandPackageName(redirect);
        }
        for (var child : node.getChildren()) {
            var childResult = commandPackageName(child);
            if (childResult != null) {
                return childResult;
            }
        }
        return null;
    }
}