package com.nexia.core.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.gui.PrefixGUI;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DetectCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(Commands.literal("detect").executes(DetectCommand::run));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executer = context.getSource().getPlayerOrException();
        List<Player> playersNearby = executer.level.getEntitiesOfClass(ServerPlayer.class,executer.getBoundingBox().inflate(12, 0.25, 12));
        Vec3 eyePos = executer.getEyePosition(1);
        AtomicReference<Vec3> nearestPosition = new AtomicReference<>();
        playersNearby.forEach(player -> {
            Vec3 currentPos = player.getBoundingBox().getNearestPointTo(eyePos);
            if(nearestPosition.get() == null || nearestPosition.get().distanceToSqr(eyePos) > currentPos.distanceToSqr(eyePos))
                nearestPosition.set(currentPos);
        });
        if(nearestPosition.get() != null) {
            Vec3 nearestPos = nearestPosition.get();
            ServerTime.factoryServer.runCommand("/player .bot look at " + nearestPos.x + " " + nearestPos.y + " " + nearestPos.z, 4, false);
        }
        return 1;
    }
}
