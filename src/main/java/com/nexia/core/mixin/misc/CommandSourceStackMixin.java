package com.nexia.core.mixin.misc;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin {

    @Shadow @Final private CommandSource source;

    @Shadow @Final private @Nullable Entity entity;

    @Inject(method = "broadcastToAdmins", cancellable = true, at = @At("HEAD"))
    private void broadcastToAdmins(Component component, CallbackInfo ci) {

        // If sourced from datapack
        if ((!(source instanceof Entity)) && entity != null) {
            ci.cancel();
            return;
        }
    }
}
