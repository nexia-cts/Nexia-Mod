package com.nexia.core.mixin.misc;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow protected abstract void removeFromChunk(Entity entity);

    @Shadow @Final private Int2ObjectMap<Entity> entitiesById;

    @Shadow public abstract void onEntityRemoved(Entity entity);

    @Inject(method = "despawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;pauseInIde(Ljava/lang/Throwable;)Ljava/lang/Throwable;"), cancellable = true)
    public void meWhenTheCrashFix(Entity entity, CallbackInfo ci) {
        this.removeFromChunk(entity);
        this.entitiesById.remove(entity.getId());
        this.onEntityRemoved(entity);
        ci.cancel();
    }
}
