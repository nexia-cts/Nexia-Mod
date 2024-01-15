package com.nexia.core.mixin.entity;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.ffa.sky.utilities.FfaAreas;
import com.nexia.minigames.games.football.FootballGame;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

    /**
     * @author NotCoded
     * @reason dadadadooo
     */
    @Overwrite
    public void push(Entity entity) {
        Entity entity1 = (Entity) (Object) this;
        if (!this.isPassengerOfSameVehicle(entity)) {
            if (!entity.noPhysics && !this.noPhysics) {
                double d = entity.getX() - this.getX();
                double e = entity.getZ() - this.getZ();
                double f = Mth.absMax(d, e);
                if (f >= 0.009999999776482582) {
                    f = (double)Mth.sqrt(f);
                    d /= f;
                    e /= f;
                    double g = 1.0 / f;
                    if (g > 1.0) {
                        g = 1.0;
                    }

                    d *= g;
                    e *= g;

                    if(!(entity1 instanceof ArmorStand && this.level.equals(FootballGame.world))) {
                        d *= 0.05000000074505806;
                    }


                    e *= 0.05000000074505806;
                    d *= (double)(1.0F - this.pushthrough);
                    e *= (double)(1.0F - this.pushthrough);
                    if (!this.isVehicle()) {
                        this.push(-d, 0.0, -e);
                    }

                    if (!entity.isVehicle()) {
                        entity.push(d, 0.0, e);
                    }
                }

            }
        }
    }

}
