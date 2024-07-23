package com.nexia.core.mixin.misc;

import com.mojang.authlib.GameProfile;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.core.utilities.time.ServerType;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public abstract PlayerList getPlayerList();

    @Unique
    boolean firstTickPassed = false;

    @Inject(at = @At("HEAD"), method = "tickChildren")
    private void tickHead(CallbackInfo ci) {
        if (!firstTickPassed) {
            firstTickPassed = true;
            ServerTime.firstTick(MinecraftServer.class.cast(this));
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
    }

    @ModifyArg(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus$Players;setSample([Lcom/mojang/authlib/GameProfile;)V"))
    private GameProfile[] hidePlayers(GameProfile[] gameProfiles) {

        if(ServerType.returnServer().equals(ServerType.DEV)) {
            return new GameProfile[]{new GameProfile(Util.NIL_UUID, "§e⟡ you tried ⟡"),
                    new GameProfile(Util.NIL_UUID, "§eヽ(・∀・)ﾉ"),
                    new GameProfile(Util.NIL_UUID, " "),
                    new GameProfile(Util.NIL_UUID, "IPs:"),
                    new GameProfile(Util.NIL_UUID, "eu.nexia.dev"),
                    new GameProfile(Util.NIL_UUID, "na.nexia.dev")
                };
        }

        return gameProfiles;
    }

    @ModifyArg(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus;setPlayers(Lnet/minecraft/network/protocol/status/ServerStatus$Players;)V"))
    private ServerStatus.Players hidePlayerCount(ServerStatus.Players players) {
        if(ServerType.returnServer().equals(ServerType.DEV)) {
            // can't set current player count for some reason (69)
            return new ServerStatus.Players(420, 69);
        }
        return players;
    }
}
