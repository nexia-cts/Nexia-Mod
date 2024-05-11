package com.nexia.core.mixin.entity;

import com.nexia.ffa.FfaUtil;
import com.nexia.minigames.games.duels.custom.kitroom.kitrooms.KitRoom;
import com.nexia.minigames.games.football.FootballGame;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity {

    @Shadow protected abstract void playBrokenSound();

    @Shadow protected abstract void brokenByAnything(DamageSource damageSource);

    @Shadow private boolean invisible;

    @Shadow public abstract boolean isMarker();

    @Shadow protected abstract void showBreakingParticles();

    @Shadow public long lastHit;

    @Shadow protected abstract void causeDamage(DamageSource damageSource, float f);

    @Shadow @Final private static Predicate<Entity> RIDABLE_MINECARTS;

    protected ArmorStandMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interactAt", cancellable = true, at = @At("HEAD"))
    private void canTakeItem(Player player, Vec3 vec3, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {

        // Disable interacting with armor stands
        if ((FfaUtil.isFfaPlayer(player) || FootballGame.isFootballPlayer(player) || KitRoom.isInKitRoom(player)) && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }



    // Football:

    /**
     * @author NotCoded
     * @reason Make armor stands pushable
     */
    @Overwrite
    public boolean isPushable() {
        // return FootballGame.world.equals(this.level);
        return false;
    }


    /**
     * @author NotCoded
     * @reason Make armor stands pushable
     */
    @Overwrite
    protected void doPush(Entity entity) {
        // if(!FootballGame.world.equals(this.level)) return;
        // entity.push(this);
        return;
    }


    /**
     * @author NotCoded
     * @reason Make armor stands pushable
     */
    @Overwrite
    protected void pushEntities() {
        if(!FootballGame.world.equals(this.level)) {
            List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);

            for (Entity value : list) {
                if (this.distanceToSqr(value) <= 0.2) {
                    value.push(this);
                }
            }
            return;
        }
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), entity -> !entity.isSpectator());
        if (!list.isEmpty()) {
            int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            int j;
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                j = 0;

                for (Entity ignored : list) {
                    ++j;
                }
            }

            for(j = 0; j < list.size(); ++j) {
                Entity k = list.get(j);
                this.doPush(k);
            }
        }

    }

    /**
     * @author NotCoded
     * @reason Prevent armor stands from getting killed when 2 players hit them at the same time.
     */
    @Overwrite
    public boolean hurt(DamageSource damageSource, float f) {
        if (!this.level.isClientSide && !this.removed) {
            if (DamageSource.OUT_OF_WORLD.equals(damageSource)) {
                this.remove();
                return false;
            } else if (!this.isInvulnerableTo(damageSource) && !this.invisible && !this.isMarker()) {
                if (damageSource.isExplosion()) {
                    this.brokenByAnything(damageSource);
                    this.remove();
                    return false;
                } else if (DamageSource.IN_FIRE.equals(damageSource)) {
                    if (this.isOnFire()) {
                        this.causeDamage(damageSource, 0.15F);
                    } else {
                        this.setSecondsOnFire(5);
                    }

                    return false;
                } else if (DamageSource.ON_FIRE.equals(damageSource) && this.getHealth() > 0.5F) {
                    this.causeDamage(damageSource, 4.0F);
                    return false;
                } else {
                    boolean bl = damageSource.getDirectEntity() instanceof AbstractArrow;
                    boolean bl2 = bl && ((AbstractArrow)damageSource.getDirectEntity()).getPierceLevel() > 0;
                    boolean bl3 = "player".equals(damageSource.getMsgId());
                    if (!bl3 && !bl) {
                        return false;
                    } else if (damageSource.getEntity() instanceof Player && !((Player)damageSource.getEntity()).abilities.mayBuild) {
                        return false;
                    } else if (damageSource.isCreativePlayer()) {
                        this.playBrokenSound();
                        this.showBreakingParticles();
                        this.remove();
                        return bl2;
                    } else {
                        long l = this.level.getGameTime();
                        if (l - this.lastHit > 5L && !bl) {
                            this.level.broadcastEntityEvent(this, (byte)32);
                            this.lastHit = l;
                        }
                        /*
                        else {
                            this.brokenByPlayer(damageSource);
                            this.showBreakingParticles();
                            this.remove();
                        }
                         */
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
