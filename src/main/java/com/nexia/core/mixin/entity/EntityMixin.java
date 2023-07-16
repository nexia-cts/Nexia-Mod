package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.LobbyUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void hurt(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        /*
        if ((damageSource == DamageSource.FALL && getTags().contains(LobbyUtil.NO_FALL_DAMAGE_TAG)) || (damageSource == DamageSource.FALL && getTags().contains("ffa"))) {
            cir.setReturnValue(true);
        }

         */
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
    private double modifyVoidY(Entity instance) {
        double voidY = -32;

        return instance.getY() + -64 - voidY;
    }

}
