package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.sky.utilities.FfaAreas;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, CommandSource {

    @Shadow public abstract Set<String> getTags();

    @Shadow public abstract boolean isPassengerOfSameVehicle(Entity entity);

    @Shadow public boolean noPhysics;

    @Shadow public abstract double getX();

    @Shadow public abstract double getZ();

    @Shadow public float pushthrough;

    @Shadow public abstract boolean isVehicle();

    @Shadow public abstract void push(double d, double e, double f);

    @Shadow public Level level;

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void hurt(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource == DamageSource.FALL && getTags().contains(LobbyUtil.NO_FALL_DAMAGE_TAG) || damageSource == DamageSource.FALL && getTags().contains("ffa")) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
    private double modifyVoidY(Entity instance) {
        double voidY = 0;

        if (instance.level instanceof ServerLevel serverLevel) {

            if (FfaAreas.isFfaWorld(serverLevel)) {
                voidY = FfaAreas.getVoidY();
            }
        }

        return instance.getY() + 32 - voidY;
    }
}
