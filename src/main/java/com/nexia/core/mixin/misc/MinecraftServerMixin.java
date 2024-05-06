package com.nexia.core.mixin.misc;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import com.nexia.core.Main;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Unique
    boolean firstTickPassed = false;

    @Inject(at = @At("HEAD"), method = "tickChildren")
    private void tickHead(CallbackInfo ci) {
        if (!firstTickPassed) {
            firstTickPassed = true;
            ServerTime.firstTick((MinecraftServer)(Object)this);
        }
    }

    @Inject(at = @At("TAIL"), method = "tickChildren")
    private void tickTail(CallbackInfo ci) {
        ServerTime.everyTick();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    protected void stopServer(CallbackInfo ci) {
        ServerTime.stopServer();
    }

    @ModifyReturnValue(method = "isUnderSpawnProtection", at = @At("HEAD"))
    private boolean noSpawnProtection() {
        return false;
    }

    @ModifyArg(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus$Players;setSample([Lcom/mojang/authlib/GameProfile;)V"))
    private GameProfile[] hidePlayers(GameProfile[] gameProfiles) {
        return (Main.config.hidePlayers) ? new GameProfile[]{} : gameProfiles;
    }

}
