package com.nexia.core.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.time.ServerTime;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DetectCommand {

    public static final boolean enabled = false;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        if(!enabled) return;
        dispatcher.register(Commands.literal("detect")
                .requires(commandSourceStack -> Permissions.check(commandSourceStack, "nexia.dev.detect", 4))
                .executes(DetectCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(!enabled) return 0;
        detect(context.getSource().getPlayerOrException());
        return 1;
    }

    public static void detect(ServerPlayer attacker) {

        String botName = ".bot";

        List<Player> playersNearby = attacker.level.getEntitiesOfClass(ServerPlayer.class, attacker.getBoundingBox().inflate(12, 0.25, 12));
        Vec3 eyePos = attacker.getEyePosition(1);
        AtomicReference<Vec3> nearestPosition = new AtomicReference<>();
        playersNearby.forEach(player -> {
            Vec3 currentPos = player.getBoundingBox().getNearestPointTo(eyePos);
            if(nearestPosition.get() == null || nearestPosition.get().distanceToSqr(eyePos) > currentPos.distanceToSqr(eyePos))
                nearestPosition.set(currentPos);
        });
        if(nearestPosition.get() != null) {
            Vec3 nearestPos = nearestPosition.get();
            ServerTime.factoryServer.runCommand("/player " + botName + " look at " + nearestPos.x + " " + nearestPos.y + " " + nearestPos.z, 4, false);
        }
    }
}
