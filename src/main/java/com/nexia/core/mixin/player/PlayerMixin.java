package com.nexia.core.mixin.player;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.misc.EventUtil;
import com.nexia.core.utilities.player.CorePlayerData;
import com.nexia.core.utilities.player.CoreSavedPlayerData;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.ffa.base.BaseFfaUtil;
import com.nexia.minigames.games.bedwars.players.BwPlayerEvents;
import com.nexia.minigames.games.bedwars.util.BwUtil;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    @Shadow public abstract ItemCooldowns getCooldowns();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }


    /**
     * @author NotCoded
     * @reason Make shield break actually play the sound to other players.
     */
    @Overwrite
    public boolean disableShield(float f) {
        this.getCooldowns().addCooldown(Items.SHIELD, (int)(f * 20.0F));
        this.stopUsingItem();
        Player player = (Player) (Object) this;
        this.level.broadcastEntityEvent(this, (byte)30);
        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player);
        if(attacker != null){
            //this.level.broadcastEntityEvent(attacker, (byte)30);

            SoundSource soundSource = null;
            for (SoundSource source : SoundSource.values()) {
                soundSource = source;
            }
            new NexiaPlayer(attacker).sendSound(new EntityPos(attacker.position()), SoundEvents.SHIELD_BREAK, soundSource, 2, 1);
        }
        return true;
    }

    @Inject(method = "canEat", cancellable = true, at = @At("HEAD"))
    private void preventFFAUsers(boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        if((com.nexia.ffa.sky.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.sky.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer))
                || (com.nexia.ffa.uhc.utilities.FfaAreas.isFfaWorld(player.level) && com.nexia.ffa.uhc.utilities.FfaAreas.isInFfaSpawn(nexiaPlayer))
                || (((CorePlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(PlayerGameMode.LOBBY) && ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(nexiaPlayer)).gameMode.equals(DuelGameMode.LOBBY))) {
            cir.setReturnValue(false);
            nexiaPlayer.refreshInventory();
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At("HEAD"))
    private void beforeHurt(DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        NexiaPlayer nexiaPlayer = new NexiaPlayer(player);

        for (BaseFfaUtil util : BaseFfaUtil.ffaUtils) {
            if (util.isFfaPlayer(nexiaPlayer) && !util.beforeDamage(nexiaPlayer, damageSource)) {
                cir.setReturnValue(false);
                return;
            }
        }

        if(player.getLevel().equals(LobbyUtil.lobbyWorld) && damageSource == DamageSource.OUT_OF_WORLD) {
            LobbyUtil.lobbySpawn.teleportPlayer(LobbyUtil.lobbyWorld, player);
            cir.setReturnValue(false);
            return;
        }

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player, damageSource.getEntity());

        if (player.getTags().contains(LobbyUtil.NO_DAMAGE_TAG)) {
            cir.setReturnValue(false);
            return;
        }

        if(attacker != null) {
            if(attacker.getTags().contains(LobbyUtil.NO_DAMAGE_TAG)) {
                cir.setReturnValue(false);
                return;
            }

            /*
            DuelsTeam team = PlayerDataManager.get(player).duelOptions.duelsTeam;
            if(team != null && team.all.contains(AccuratePlayer.create(attacker)) && PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(player).gameMode == PlayerGameMode.LOBBY) {
                cir.setReturnValue(false);
            }
            */
        }
    }

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


    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
    public boolean setSprintFix(boolean par1) {
        return ((CoreSavedPlayerData)PlayerDataManager.getDataManager(NexiaCore.CORE_DATA_MANAGER).get(new NexiaPlayer((ServerPlayer) (Object) this)).savedData).isSprintFix();
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void shieldBlockExplosion(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource.isExplosion() && this.useItem.getItem() == Items.SHIELD) {
            this.hurtCurrentlyUsedShield(f);
            cir.setReturnValue(false);
        }
    }
}
