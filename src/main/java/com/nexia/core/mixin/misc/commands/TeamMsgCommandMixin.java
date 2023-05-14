package com.nexia.core.mixin.misc.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TeamMsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
    @Inject(method = "register", cancellable = true, at = @At("HEAD"))
    private static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CallbackInfo ci) {
        // Fuck you v1.3
        ci.cancel();
    }
}