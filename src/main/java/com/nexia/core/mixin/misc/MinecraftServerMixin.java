package com.nexia.core.mixin.misc;

import com.mojang.authlib.GameProfile;
import com.nexia.core.Main;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "isUnderSpawnProtection", at = @At("HEAD"), cancellable = true)
    private void noSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        return;
    }

    @ModifyArg(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus$Players;setSample([Lcom/mojang/authlib/GameProfile;)V"))
    private GameProfile[] hidePlayers(GameProfile[] gameProfiles) {
        return (Main.config.hidePlayers) ? new GameProfile[]{} : gameProfiles;
    }

}
