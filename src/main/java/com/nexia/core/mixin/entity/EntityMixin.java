package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.LobbyUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, CommandSource {

    @Shadow public abstract Set<String> getTags();

    @Shadow public abstract Vec3 getDeltaMovement();

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void hurt(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource == DamageSource.FALL && getTags().contains(LobbyUtil.NO_FALL_DAMAGE_TAG) || damageSource == DamageSource.FALL && getTags().contains("ffa")) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
    private double modifyVoidY(Entity instance) {
        double voidY = -32;

        if (instance.level instanceof ServerLevel) {
        }

        return instance.getY() + -64 - voidY;
    }

}
