package com.nexia.minigames.games.bedwars.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class BwExplosiveSlime extends Slime {

    public int age;
    public LivingEntity owner;

    public static int explodeTime = 47;

    public static boolean isBwExplosiveSlime(ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().getBoolean("BedWarsExplosiveSlime");
    }

    public BwExplosiveSlime(EntityType<? extends Slime> entityType, ServerPlayer player) {
        super(entityType, player.getLevel());
        this.age = 0;
        this.owner = player;

        this.setSize(1, true);

        float x = -Mth.sin((float) Math.toRadians(player.yRot)) * Mth.cos((float) Math.toRadians(player.xRot));
        float z = Mth.cos((float) Math.toRadians(player.yRot)) * Mth.cos((float) Math.toRadians(player.xRot));
        float y = -Mth.sin((float) Math.toRadians(player.xRot));

        float distance = 0.5f;
        Vec3 playerPos = player.getEyePosition(1).add(distance * x, distance * y - 0.25, distance * z);
        this.moveTo(playerPos);

        float throwPower = 2.4f;
        this.setDeltaMovement(player.getDeltaMovement().add(throwPower * x, throwPower * y + 0.1, throwPower * z));

        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100000, 4, false, false, false));

        player.getLevel().addFreshEntity(this);
    }

    public void tick() {
        this.age++;

        if (age == 1) {
            this.hasImpulse = true;
        }

        if (this.age >= explodeTime) {
            this.explode();
        }

        super.tick();
    }

    public void explode() {
        this.remove();
        float power = 3f;
        this.level.explode(this, this.getX(), this.getY(), this.getZ(), power, Explosion.BlockInteraction.BREAK);
    }

    public void knockback(float y, double x, double z) {
        float multiplier = 1.6f;
        super.knockback(multiplier * y, multiplier * x, multiplier * z);
    }

    protected void registerGoals() {}

}
