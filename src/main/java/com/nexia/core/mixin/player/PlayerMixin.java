package com.nexia.core.mixin.player;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import com.nexia.ffa.sky.utilities.FfaSkyUtil;
import com.nexia.ffa.uhc.utilities.FfaUhcUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow @Final public Inventory inventory;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "canEat", cancellable = true, at = @At("HEAD"))
    private void preventFFAUsers(boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if((FfaSkyUtil.INSTANCE.isFfaWorld(player.level) && FfaSkyUtil.INSTANCE.isInFfaSpawn(nexiaPlayer))
                || (FfaUhcUtil.INSTANCE.isFfaWorld(player.level) && FfaUhcUtil.INSTANCE.isInFfaSpawn(nexiaPlayer))
                || (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(PlayerGameMode.LOBBY) && ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(DuelGameMode.LOBBY))) {
            cir.setReturnValue(false);
            nexiaPlayer.refreshInventory();
        }
    }

    // Make void death instant
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void afterHurt(DamageSource damageSource, float damage, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            BwPlayerEvents.afterHurt(nexiaPlayer, damageSource);
        }
    }

    @Redirect(method = "actuallyHurt",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float modifyArmorCalculation(Player instance, DamageSource damageSource, float damage) {

        hurtArmor(damageSource, damage);
        if (!((Object) this instanceof ServerPlayer player)) return vanillaArmorCalculation(damageSource, damage);

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            return BwUtil.playerArmorCalculation(player, damageSource, damage);
        }

        return vanillaArmorCalculation(damageSource, damage);
    }

    @Unique
    public float vanillaArmorCalculation(DamageSource damageSource, float damage) {
        if (!damageSource.isBypassArmor()) {
            damage = CombatRules.getDamageAfterAbsorb(damage, this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        return damage;
    }


    @Inject(method = "drop(Z)Z", cancellable = true, at = @At("HEAD"))
    private void drop1(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        ItemStack dropped = inventory.getItem(inventory.selected);

        if (!EventUtil.dropItem(nexiaPlayer, dropped)) {
            cir.setReturnValue(false);
            nexiaPlayer.refreshInventory();
        }

    }

    @Inject(method = "getAttackDelay", at = @At("HEAD"))
    private void getAttackDelay(CallbackInfoReturnable<Integer> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if (BwUtil.isBedWarsPlayer(nexiaPlayer)) {
            BwUtil.setAttackSpeed(player);
        }
    }
}
