package com.nexia.core.commands.staff;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.commands.CommandUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DetectCommand {

    public static final boolean enabled = false;

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        if(!enabled) return;
        dispatcher.register(CommandUtils.literal("detect")
                .requires(commandSourceInfo -> CommandUtil.hasPermission(commandSourceInfo, "nexia.dev.detect", 4))
                .executes(DetectCommand::run)
        );
    }

    public static int run(CommandContext<CommandSourceInfo> context) throws CommandSyntaxException {
        if(!enabled) return 0;

        detect(new NexiaPlayer(context.getSource().getPlayerOrException()));
        return 1;
    }

    public static void detect(NexiaPlayer attacker) {

        String botName = ".bot";

        List<Player> playersNearby = attacker.unwrap().level.getEntitiesOfClass(ServerPlayer.class, attacker.unwrap().getBoundingBox().inflate(12, 0.25, 12));
        Vec3 eyePos = attacker.unwrap().getEyePosition(1);
        AtomicReference<Vec3> nearestPosition = new AtomicReference<>();
        playersNearby.forEach(player -> {
            Vec3 currentPos = player.getBoundingBox().getNearestPointTo(eyePos);
            if(nearestPosition.get() == null || nearestPosition.get().distanceToSqr(eyePos) > currentPos.distanceToSqr(eyePos))
                nearestPosition.set(currentPos);
        });
        if(nearestPosition.get() != null) {
            Vec3 nearestPos = nearestPosition.get();
            ServerTime.nexusServer.runCommand("/player " + botName + " look at " + nearestPos.x + " " + nearestPos.y + " " + nearestPos.z, 4, false);
        }
    }
}
