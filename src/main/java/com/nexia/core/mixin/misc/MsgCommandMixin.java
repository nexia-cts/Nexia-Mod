package com.nexia.core.mixin.misc;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.MsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {

    @Inject(method = "register", cancellable = true, at = @At("HEAD"))
    private static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CallbackInfo ci) {
        // Fuck you
        ci.cancel();
    }

}
